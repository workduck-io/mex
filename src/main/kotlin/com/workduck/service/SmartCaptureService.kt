package com.workduck.service

import com.fasterxml.jackson.core.type.TypeReference
import com.serverless.models.requests.MoveEntityRequest
import com.serverless.models.requests.EntityTypeRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidCaptureID
import com.workduck.models.AdvancedElement
import com.workduck.models.EntityOperationType
import com.workduck.models.EntityServiceCreateResponse
import com.workduck.models.Node
import com.workduck.models.entityServiceResponses.MultipleEntityPaginatedResponse
import com.workduck.models.entityServiceResponses.SingleEntityResponse
import com.workduck.service.serviceUtils.ServiceUtils.getNodeIDWorkspaceID
import com.workduck.service.serviceUtils.ServiceUtils.populateEntityMetadata
import com.workduck.service.serviceUtils.ServiceUtils.invokeCreateOrUpdateEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeUpdateEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeDeleteEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetEntitiesWithFilterLambda
import com.workduck.utils.EntityHelper
import com.workduck.utils.Helper
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

class SmartCaptureService (
    private val nodeService : NodeService = NodeService()
){
    fun createSmartCapture(wdRequest: WDRequest, userID: String, userWorkspaceID: String) : String {
        val request = wdRequest as EntityTypeRequest

        // this contains the nodeID to which smartCapture should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)

        val smartCapture: AdvancedElement = request.data!!
        populateEntityMetadata(smartCapture, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)

        val captureID = invokeCreateOrUpdateEntityLambda(
                            smartCapture,
                            nodeWorkspaceMap.workspaceID,
                            userID,
                            LambdaFunctionNames.CAPTURE_LAMBDA,
                            RoutePaths.CREATE_CAPTURE,
                            HttpMethods.POST).id
        val refBlock = EntityHelper.createEntityReferenceBlock(smartCapture.id, captureID, Constants.ELEMENT_SMART_CAPTURE)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))
        return captureID
    }


    fun updateSmartCapture(smartCaptureID: String, wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as EntityTypeRequest

        // this contains the nodeID to which smartCapture should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)

        val smartCapture: AdvancedElement = request.data!!

        populateEntityMetadata(smartCapture, userID, createdAt = null, createdBy = null)
        invokeUpdateEntityLambda(
            smartCaptureID,
            smartCapture,
            nodeWorkspaceMap.workspaceID,
            userID,
            LambdaFunctionNames.CAPTURE_LAMBDA,
            RoutePaths.UPDATE_CAPTURE,
            HttpMethods.PATCH)
    }

    fun getSmartCapture(captureID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String): AdvancedElement? {
        val workspaceID = nodeService.nodeAccessService
            .checkIfUserHasAccessAndGetWorkspaceDetails(
                nodeID,
                userWorkspaceID,
                namespaceID,
                userID,
                EntityOperationType.WRITE
            ).let { workspaceDetails ->
                require(!workspaceDetails[Constants.WORKSPACE_ID].isNullOrEmpty()) { Messages.ERROR_NODE_PERMISSION }
                workspaceDetails[Constants.WORKSPACE_ID]!!
            }

        return Helper.objectMapper.readValue(
            invokeGetEntityLambda(
                workspaceID,
                userID,
                captureID,
                LambdaFunctionNames.CAPTURE_LAMBDA,
                RoutePaths.GET_CAPTURE,
                HttpMethods.GET),
                SingleEntityResponse::class.java
                ).data
    }


    fun deleteSmartCapture(captureID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String) {
        val workspaceID = nodeService.nodeAccessService
            .checkIfUserHasAccessAndGetWorkspaceDetails(
                nodeID,
                userWorkspaceID,
                namespaceID,
                userID,
                EntityOperationType.WRITE
            ).let { workspaceDetails ->
                require(!workspaceDetails[Constants.WORKSPACE_ID].isNullOrEmpty()) { Messages.ERROR_NODE_PERMISSION }
                workspaceDetails[Constants.WORKSPACE_ID]!!
            }

        invokeDeleteEntityLambda(
            workspaceID,
            userID,
            captureID,
            LambdaFunctionNames.CAPTURE_LAMBDA,
            RoutePaths.DELETE_CAPTURE,
            HttpMethods.DELETE
        )
    }

    /* this endpoint is just to move smart capture from default capture node in user's own workspace */
    fun moveSmartCapture(wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as MoveEntityRequest
        val captureID = request.entityID
        require(captureID.isValidCaptureID()) { Messages.INVALID_CAPTURE_ID }


        // this map contains the nodeID to which smartCapture should be moved and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)

        //TODO(ask directly for blockID from entity service)

        val blockID = Helper.objectMapper.readValue(
            invokeGetEntityLambda(
                userWorkspaceID,
                userID,
                captureID,
                LambdaFunctionNames.CAPTURE_LAMBDA,
                RoutePaths.GET_CAPTURE,
                HttpMethods.GET),
                SingleEntityResponse::class.java).data.id

        val sourceNodeWithBlockAndDataOrder: Node = nodeService.nodeRepository.getNodeWithBlockAndDataOrder(Constants.SMART_CAPTURE_DEFAULT_NODE_ID, blockID, userWorkspaceID).let{ node ->
            require(node != null) { Messages.INVALID_NODE_ID }
            require(node.data?.get(0) != null ) { Messages.INVALID_BLOCK_ID }
            check(!node.dataOrder.isNullOrEmpty()) {Messages.INVALID_NODE_STATE}
            node
        }

        sourceNodeWithBlockAndDataOrder.dataOrder!!.let { dataOrder ->
            nodeService.nodeRepository.moveBlock(sourceNodeWithBlockAndDataOrder.data!![0], userWorkspaceID, Constants.SMART_CAPTURE_DEFAULT_NODE_ID, nodeWorkspaceMap.workspaceID, nodeWorkspaceMap.nodeID, dataOrder)
        }
    }

    fun getAllSmartCapturesForFilter(workspaceID: String, userID: String, filterType: String, filterValue: String, lastKey: String?): MultipleEntityPaginatedResponse<SingleEntityResponse> {
        return Helper.objectMapper.readValue(
            invokeGetEntitiesWithFilterLambda(
                        workspaceID,
                        userID,
                        filterType,
                        filterValue,
                        LambdaFunctionNames.CAPTURE_LAMBDA,
                        RoutePaths.GET_ALL_CAPTURES_WITH_FILTER,
                        HttpMethods.GET,
                        lastKey), object : TypeReference<MultipleEntityPaginatedResponse<SingleEntityResponse>>() {})
    }
}
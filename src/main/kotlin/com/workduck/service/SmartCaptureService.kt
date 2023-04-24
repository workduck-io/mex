package com.workduck.service

import com.amazonaws.services.lambda.model.InvocationType
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.MoveEntityRequest
import com.serverless.models.requests.NodeNamespaceMap
import com.serverless.models.requests.NodeWorkspaceMap
import com.serverless.models.requests.SmartCaptureRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidCaptureID
import com.workduck.models.AdvancedElement
import com.workduck.models.EntityOperationType
import com.workduck.models.EntityServiceCreateResponse
import com.workduck.models.Node
import com.workduck.models.entityServiceResponses.MultipleEntityResponse
import com.workduck.models.entityServiceResponses.SingleEntityResponse
import com.workduck.models.externalRequests.ExternalRequestHeader
import com.workduck.models.externalRequests.RequestContext
import com.workduck.utils.EntityHelper
import com.workduck.utils.Helper
import com.workduck.utils.LambdaHelper
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

class SmartCaptureService (
    private val objectMapper: ObjectMapper = Helper.objectMapper,

    private val nodeService : NodeService = NodeService()
){

    fun createSmartCapture(wdRequest: WDRequest, userID: String, userWorkspaceID: String) : String {
        val request = wdRequest as SmartCaptureRequest

        // this contains the nodeID to which smartCapture should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(request.nodeNamespaceMap, userID, userWorkspaceID)

        val smartCapture: AdvancedElement = request.data
        populateSmartCaptureMetadata(smartCapture, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)

        val captureID = invokeCreateCaptureLambda(smartCapture, nodeWorkspaceMap.workspaceID, userID).id

        val refBlock = EntityHelper.createEntityReferenceBlock(smartCapture.id, captureID, Constants.ELEMENT_SMART_CAPTURE)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))
        println(captureID)
        return captureID
    }


    fun updateSmartCapture(smartCaptureID: String, wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as SmartCaptureRequest

        // this contains the nodeID to which smartCapture should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(request.nodeNamespaceMap, userID, userWorkspaceID)

        val smartCapture: AdvancedElement = request.data

        populateSmartCaptureMetadata(smartCapture, userID, createdAt = null, createdBy = null)
        invokeUpdateCaptureLambda(smartCaptureID, smartCapture, nodeWorkspaceMap.workspaceID, userID)


    }

    fun getSmartCapture(captureID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String): AdvancedElement {

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

        return invokeGetCaptureLambda(workspaceID, userID, captureID).data
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

        invokeDeleteCaptureLambda(workspaceID, userID, captureID)
    }

    fun getAllSmartCapturesForFilter(workspaceID: String, userID: String, filterType: String, filterValue: String): MultipleEntityResponse {
        return invokeGetCapturesWithFilterLambda(workspaceID, userID, filterType, filterValue)

    }

    /* this endpoint is just to move smart capture from default capture node in user's own workspace */
    fun moveSmartCapture(wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as MoveEntityRequest
        val captureID = request.entityID
        require(captureID.isValidCaptureID()) { Messages.INVALID_CAPTURE_ID }


        // this map contains the nodeID to which smartCapture should be moved and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(request.nodeNamespaceMap, userID, userWorkspaceID)

        //TODO(ask directly for blockID from entity service)
        val blockID = invokeGetCaptureLambda(userWorkspaceID, userID, captureID).data.id

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


    private fun getNodeIDWorkspaceID(nodeNamespaceMap: NodeNamespaceMap?, userID: String, userWorkspaceID: String) : NodeWorkspaceMap {

        return when(nodeNamespaceMap == null) {
            true -> { /* if no node,namespace is given we will add the smart capture to user's workspace in default place. */
                NodeWorkspaceMap(
                    nodeID = Constants.SMART_CAPTURE_DEFAULT_NODE_ID,
                    workspaceID = userWorkspaceID
                )
            }

            false -> {
                val nodeID = nodeNamespaceMap.nodeID
                val namespaceID = nodeNamespaceMap.namespaceID
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
                NodeWorkspaceMap(
                    nodeID = nodeID,
                    workspaceID = workspaceID
                )

            }
        }

    }


    private fun invokeCreateCaptureLambda(smartCapture: AdvancedElement, workspaceID: String, userID: String) : EntityServiceCreateResponse{
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.CREATE_CAPTURE, HttpMethods.POST)
        val requestBody = objectMapper.writeValueAsString(EntityHelper.createEntityPayload(smartCapture))
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, requestBody = requestBody)

        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)

    }

    private fun invokeUpdateCaptureLambda(captureID: String, smartCapture: AdvancedElement, workspaceID: String, userID: String){
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.UPDATE_CAPTURE, HttpMethods.PATCH)
        val requestBody = objectMapper.writeValueAsString(EntityHelper.createEntityPayload(smartCapture))
        val pathParameters : Map<String, String> = mapOf("id" to captureID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, requestBody = requestBody, pathParameters = pathParameters)
    }

    private fun invokeGetCaptureLambda(workspaceID: String, userID: String, captureID: String) : SingleEntityResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.GET_CAPTURE, HttpMethods.GET)
        val pathParameters : Map<String, String> = mapOf("id" to captureID)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, pathParameters = pathParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)

    }

    private fun invokeDeleteCaptureLambda(workspaceID: String, userID: String, captureID: String){
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.DELETE_CAPTURE, HttpMethods.DELETE)
        val pathParameters : Map<String, String> = mapOf("id" to captureID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, pathParameters = pathParameters)
    }

    private fun invokeGetCapturesWithFilterLambda(workspaceID: String, userID: String, filterType: String, filterValue: String)  : MultipleEntityResponse{
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.GET_ALL_CAPTURES_WITH_FILTER, HttpMethods.GET)
        val queryStringParameters : Map<String, String> = mapOf("filterType" to filterType, "filterValue" to filterValue)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, queryStringParameters = queryStringParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        val singleEntityResponseList: List<SingleEntityResponse> = Helper.objectMapper.readValue(jsonBody, object : TypeReference<List<SingleEntityResponse>>() {})
        return MultipleEntityResponse(entities = singleEntityResponseList)
    }


    private fun populateSmartCaptureMetadata(smartCapture: AdvancedElement, userID: String, createdAt : Long?, createdBy : String?){
        smartCapture.createdBy = createdBy
        smartCapture.createdAt = createdAt
        smartCapture.lastEditedBy = userID
        smartCapture.updatedAt = createdAt?: Constants.getCurrentTime()

    }

}
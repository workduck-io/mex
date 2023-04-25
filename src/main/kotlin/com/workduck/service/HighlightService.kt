package com.workduck.service

import com.serverless.models.requests.*
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AdvancedElement
import com.workduck.models.EntityOperationType
import com.workduck.models.entityServiceResponses.MultipleEntityPaginatedResponse
import com.workduck.models.entityServiceResponses.MultipleEntityResponse
import com.workduck.service.serviceUtils.ServiceUtils.populateEntityMetadata
import com.workduck.service.serviceUtils.ServiceUtils.invokeCreateOrUpdateEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.getNodeIDWorkspaceID
import com.workduck.service.serviceUtils.ServiceUtils.invokeCreateInstanceEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeDeleteEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntitiesByIDSLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntityByIDLambda
import com.workduck.utils.EntityHelper
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

class HighlightService(
    private val nodeService : NodeService = NodeService()
){

    fun createHighlight(wdRequest: WDRequest, userID: String, userWorkspaceID: String) : String {
        val request = wdRequest as EntityTypeRequest
        // this contains the nodeID to which highlight should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val highlight: AdvancedElement = request.data
        populateEntityMetadata(highlight, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)
        val highlightID = invokeCreateOrUpdateEntityLambda(
                            highlight,
                            nodeWorkspaceMap.workspaceID,
                            userID,
                            LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                            RoutePaths.CREATE_OR_UPDATE_HIGHLIGHT,
                            HttpMethods.POST).id
        val refBlock = EntityHelper.createEntityReferenceBlock(highlight.id, highlightID, Constants.ELEMENT_HIGHLIGHT)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))

        return highlightID
    }

    fun createHighlightInstance(wdRequest: WDRequest, userID: String, userWorkspaceID: String, highlightID: String) : String {
        val request = wdRequest as HighlightInstanceRequest
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val highlightInstanceID = invokeCreateInstanceEntityLambda(
            highlightID,
            nodeWorkspaceMap.workspaceID,
            userID,
            LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            RoutePaths.CREATE_HIGHLIGHT_INSTANCE,
            HttpMethods.POST).id
        val refBlock = EntityHelper.createEntityReferenceBlock(Constants.DEFAULT_VALUE, highlightInstanceID, Constants.ELEMENT_HIGHLIGHT)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))

        return highlightInstanceID
    }

    fun updateHighlight(highlightID: String, wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as EntityTypeRequest
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val highlight: AdvancedElement = request.data
        populateEntityMetadata(highlight, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)
        invokeCreateOrUpdateEntityLambda(
            highlight,
            nodeWorkspaceMap.workspaceID,
            userID,
            LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            RoutePaths.CREATE_OR_UPDATE_HIGHLIGHT,
            HttpMethods.POST,
            entityID = highlightID).id
    }

    fun getHighlight(highlightID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String): AdvancedElement? {
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

        return invokeGetEntityLambda(
                workspaceID,
                userID,
                highlightID,
                LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                RoutePaths.GET_HIGHLIGHT,
                HttpMethods.GET).data
    }


    fun deleteHighlight(highlightID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String) {
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
            highlightID,
            LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            RoutePaths.DELETE_HIGHLIGHT,
            HttpMethods.DELETE
        )
    }

    fun getAllHighlights(workspaceID: String, userID: String, lastKey: String?): MultipleEntityPaginatedResponse {
        return invokeGetAllEntityLambda(
                workspaceID,
                userID,
                LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                RoutePaths.GET_ALL_HIGHLIGHTS,
                HttpMethods.GET,
                lastKey)
    }

    fun getAllHighlightInstances(workspaceID: String, userID: String, highlightID: String): MultipleEntityPaginatedResponse {
        return invokeGetAllEntityByIDLambda(
            highlightID,
            workspaceID,
            userID,
            LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            RoutePaths.GET_ALL_HIGHLIGHT_INSTANCES,
            HttpMethods.GET)
    }

    fun getAllHighlightsByIDs(wdRequest: WDRequest, userID: String, userWorkspaceID: String): MultipleEntityResponse {
        val request = wdRequest as GenericListRequest

        return invokeGetAllEntitiesByIDSLambda(
                request,
                userWorkspaceID,
                userID,
                LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                RoutePaths.GET_MULTIPLE_HIGHLIGHTS,
                HttpMethods.POST)
    }
}
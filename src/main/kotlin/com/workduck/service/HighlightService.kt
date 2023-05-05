package com.workduck.service

import com.fasterxml.jackson.core.type.TypeReference
import com.serverless.models.requests.*
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AdvancedElement
import com.workduck.models.EntityOperationType
import com.workduck.models.entityServiceResponses.MultipleEntityPaginatedResponse
import com.workduck.service.serviceUtils.ServiceUtils.populateEntityMetadata
import com.workduck.service.serviceUtils.ServiceUtils.invokeCreateOrUpdateEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.getNodeIDWorkspaceID
import com.workduck.service.serviceUtils.ServiceUtils.invokeCreateInstanceEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeDeleteEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntitiesByIDSLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntityInstancesByIDLambda
import com.workduck.utils.EntityHelper
import com.workduck.utils.Helper
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

class HighlightService(
    private val nodeService : NodeService = NodeService()
){
    fun createHighlight(wdRequest: WDRequest?, userID: String, userWorkspaceID: String, parentHighlightID: String?) : String {
        val request = wdRequest as EntityTypeRequest
        // this contains the nodeID to which highlight should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val defaultBlockID: String
        val newHighlightID: String

        // This block handles the usual highlight creation
        if(parentHighlightID == null) {
            val highlight: AdvancedElement = request.data!!
            populateEntityMetadata(highlight, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)
            newHighlightID = invokeCreateOrUpdateEntityLambda(
                highlight,
                nodeWorkspaceMap.workspaceID,
                userID,
                LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                RoutePaths.CREATE_OR_UPDATE_OR_INSTANTIATE_HIGHLIGHT,
                HttpMethods.POST).id
            defaultBlockID = highlight.id
        }
        // This block handles the highlight instance creation from the given the parent highlight id
        else {
            newHighlightID = invokeCreateInstanceEntityLambda(
                parentHighlightID,
                nodeWorkspaceMap.workspaceID,
                userID,
                LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                RoutePaths.CREATE_OR_UPDATE_OR_INSTANTIATE_HIGHLIGHT,
                HttpMethods.POST,
                request.data).id
            defaultBlockID = Constants.DEFAULT_VALUE
        }
        val refBlock = EntityHelper.createEntityReferenceBlock(defaultBlockID, newHighlightID, Constants.ELEMENT_HIGHLIGHT)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))

        return newHighlightID
    }

    fun updateHighlight(highlightID: String, wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as EntityTypeRequest
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val highlight: AdvancedElement = request.data!!
        populateEntityMetadata(highlight, userID, createdAt = null, createdBy = null)
        invokeCreateOrUpdateEntityLambda(
            highlight,
            nodeWorkspaceMap.workspaceID,
            userID,
            LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            RoutePaths.CREATE_OR_UPDATE_OR_INSTANTIATE_HIGHLIGHT,
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
        return Helper.objectMapper.readValue(
                invokeGetEntityLambda(
                    workspaceID,
                    userID,
                    highlightID,
                    LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                    RoutePaths.GET_HIGHLIGHT,
                    HttpMethods.GET
                ),
                AdvancedElement::class.java
                )
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

    fun getAllHighlights(workspaceID: String, userID: String, lastKey: String? = null): MultipleEntityPaginatedResponse<AdvancedElement> {
        return Helper.objectMapper.readValue(
            invokeGetAllEntityLambda(
                    workspaceID,
                    userID,
                    LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                    RoutePaths.GET_ALL_HIGHLIGHTS,
                    HttpMethods.GET,
                    lastKey), object : TypeReference<MultipleEntityPaginatedResponse<AdvancedElement>>() {})
    }

    fun getAllHighlightInstances(workspaceID: String, userID: String, highlightID: String): MultipleEntityPaginatedResponse<AdvancedElement> {
        return Helper.objectMapper.readValue(
            invokeGetAllEntityInstancesByIDLambda(
                highlightID,
                workspaceID,
                userID,
                LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                RoutePaths.GET_ALL_HIGHLIGHT_INSTANCES,
                HttpMethods.GET), object : TypeReference<MultipleEntityPaginatedResponse<AdvancedElement>>() {})
    }

    fun getAllHighlightsByIDs(wdRequest: WDRequest, userID: String, userWorkspaceID: String): MultipleEntityPaginatedResponse<AdvancedElement> {
        val request = wdRequest as GenericListRequest

        return Helper.objectMapper.readValue(
            invokeGetAllEntitiesByIDSLambda(
                    request,
                    userWorkspaceID,
                    userID,
                    LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                    RoutePaths.GET_MULTIPLE_HIGHLIGHTS,
                    HttpMethods.POST), object : TypeReference<MultipleEntityPaginatedResponse<AdvancedElement>>() {})
    }
}
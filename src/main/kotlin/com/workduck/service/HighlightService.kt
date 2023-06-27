package com.workduck.service

import com.serverless.models.requests.*
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidHighlightID
import com.workduck.models.AdvancedElement
import com.workduck.models.entityServiceResponses.MultipleEntityPaginatedResponse
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

class HighlightService(): EntityService() {
    fun createHighlight(wdRequest: WDRequest, userID: String, userWorkspaceID: String, parentHighlightID: String?) : String {
        // This block handles the usual highlight creation
        return when(parentHighlightID) {
            null -> {
                val highlightID = createOrUpdateEntity(
                    wdRequest = wdRequest,
                    userID = userID,
                    userWorkspaceID = userWorkspaceID,
                    lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                    routePath = RoutePaths.CREATE_OR_UPDATE_OR_INSTANTIATE_HIGHLIGHT,
                    httpMethod = HttpMethods.POST,
                    elementType = Constants.ELEMENT_HIGHLIGHT,
                    entityID = null
                )
                highlightID!!
            }
            // This block handles the highlight instance creation from the given the parent highlight id
            else -> createEntityInstance(
                wdRequest = wdRequest,
                userID = userID,
                userWorkspaceID = userWorkspaceID,
                lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
                routePath = RoutePaths.CREATE_OR_UPDATE_OR_INSTANTIATE_HIGHLIGHT,
                httpMethod = HttpMethods.POST,
                elementType = Constants.ELEMENT_HIGHLIGHT,
                parentID = parentHighlightID
            )
        }
    }

    fun updateHighlight(highlightID: String, wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        createOrUpdateEntity(
            wdRequest =  wdRequest,
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            routePath = RoutePaths.CREATE_OR_UPDATE_OR_INSTANTIATE_HIGHLIGHT,
            httpMethod = HttpMethods.POST,
            elementType = Constants.ELEMENT_HIGHLIGHT,
            entityID = highlightID
        )
    }

    fun getHighlight(highlightID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String): AdvancedElement? {
        return getEntity(
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            nodeID = nodeID,
            namespaceID = namespaceID,
            lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            routePath = RoutePaths.GET_HIGHLIGHT,
            httpMethod = HttpMethods.GET,
            entityID = highlightID
        )
    }


    fun deleteHighlight(highlightID: String, nodeID: String?, namespaceID: String?, userWorkspaceID: String, userID: String) {
        deleteEntity(
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            nodeID = nodeID,
            namespaceID = namespaceID,
            lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            routePath = RoutePaths.DELETE_HIGHLIGHT,
            httpMethod = HttpMethods.DELETE,
            entityID = highlightID
        )
    }

    /* this endpoint is just to move highlight from default highlight node in user's own workspace */
    fun moveHighlight(wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as MoveEntityRequest
        val highlightID = request.entityID
        require(highlightID.isValidHighlightID()) { Messages.INVALID_HIGHLIGHT_ID }
        moveEntity(
            request = request,
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            routePath = RoutePaths.GET_HIGHLIGHT,
            httpMethod = HttpMethods.GET,
            defaultNodeID = Constants.HIGHLIGHT_DEFAULT_NODE_ID,
            entityID = highlightID
        )
    }

    fun getAllHighlights(workspaceID: String, userID: String, lastKey: String? = null): MultipleEntityPaginatedResponse<AdvancedElement> {
        return getAllEntitiesForWorkspace(
            userID = userID,
            userWorkspaceID = workspaceID,
            lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            routePath = RoutePaths.GET_ALL_HIGHLIGHTS,
            httpMethod = HttpMethods.GET,
            lastKey
        )
    }

    fun getAllHighlightInstances(workspaceID: String, userID: String, highlightID: String): MultipleEntityPaginatedResponse<AdvancedElement> {
        return getAllEntityInstances(
            userID,
            userWorkspaceID = workspaceID,
            lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            routePath = RoutePaths.GET_ALL_HIGHLIGHT_INSTANCES,
            httpMethod = HttpMethods.GET,
            parentID = highlightID
        )
    }

    fun getAllHighlightsByIDs(wdRequest: WDRequest, userID: String, userWorkspaceID: String): MultipleEntityPaginatedResponse<AdvancedElement> {
        return getAllEntitiesByIDs(
            wdRequest = wdRequest,
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            lambdaFunctionName = LambdaFunctionNames.HIGHLIGHT_LAMBDA,
            routePath = RoutePaths.GET_MULTIPLE_HIGHLIGHTS,
            httpMethod = HttpMethods.POST
        )
    }
}
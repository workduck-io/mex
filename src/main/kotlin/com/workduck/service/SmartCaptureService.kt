package com.workduck.service

import com.serverless.models.requests.MoveEntityRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidCaptureID
import com.workduck.models.AdvancedElement
import com.workduck.models.entityServiceResponses.MultipleEntityPaginatedResponse
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

class SmartCaptureService (): EntityService(){
    fun createSmartCapture(wdRequest: WDRequest, userID: String, userWorkspaceID: String) : String {
        val captureID = createOrUpdateEntity(
            wdRequest = wdRequest,
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            lambdaFunctionName = LambdaFunctionNames.CAPTURE_LAMBDA,
            routePath = RoutePaths.CREATE_CAPTURE,
            httpMethod = HttpMethods.POST,
            elementType = Constants.ELEMENT_SMART_CAPTURE,
            entityID = null
        )
        return captureID!!
    }

    fun updateSmartCapture(smartCaptureID: String, wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        updateEntity(
            wdRequest =  wdRequest,
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            lambdaFunctionName = LambdaFunctionNames.CAPTURE_LAMBDA,
            routePath = RoutePaths.UPDATE_CAPTURE,
            httpMethod = HttpMethods.PATCH,
            entityID = smartCaptureID
        )
    }

    fun getSmartCapture(captureID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String): AdvancedElement? {
        return getEntity(
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            nodeID = nodeID,
            namespaceID = namespaceID,
            lambdaFunctionName = LambdaFunctionNames.CAPTURE_LAMBDA,
            routePath = RoutePaths.GET_CAPTURE,
            httpMethod = HttpMethods.GET,
            entityID = captureID
        )
    }


    fun deleteSmartCapture(captureID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String) {
        deleteEntity(
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            nodeID = nodeID,
            namespaceID = namespaceID,
            lambdaFunctionName = LambdaFunctionNames.CAPTURE_LAMBDA,
            routePath = RoutePaths.DELETE_CAPTURE,
            httpMethod = HttpMethods.DELETE,
            entityID = captureID
        )
    }

    /* this endpoint is just to move smart capture from default capture node in user's own workspace */
    fun moveSmartCapture(wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as MoveEntityRequest
        val captureID = request.entityID
        require(captureID.isValidCaptureID()) { Messages.INVALID_CAPTURE_ID }
        moveEntity(
            request = request,
            userID = userID,
            userWorkspaceID = userWorkspaceID,
            lambdaFunctionName = LambdaFunctionNames.CAPTURE_LAMBDA,
            routePath = RoutePaths.GET_CAPTURE,
            httpMethod = HttpMethods.GET,
            defaultNodeID = Constants.SMART_CAPTURE_DEFAULT_NODE_ID,
            entityID = captureID
        )
    }

    fun getAllSmartCapturesForFilter(workspaceID: String, userID: String, filterType: String, filterValue: String, lastKey: String?): MultipleEntityPaginatedResponse<AdvancedElement> {
        return getAllEntitiesForFilter(
            userID = userID,
            userWorkspaceID = workspaceID,
            lambdaFunctionName = LambdaFunctionNames.CAPTURE_LAMBDA,
            routePath = RoutePaths.GET_ALL_CAPTURES_WITH_FILTER,
            httpMethod = HttpMethods.GET,
            filterType = filterType,
            filterValue = filterValue,
            lastKey = lastKey
        )
    }
}
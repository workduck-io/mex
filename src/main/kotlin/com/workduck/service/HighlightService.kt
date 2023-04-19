package com.workduck.service

import com.amazonaws.services.lambda.model.InvocationType
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.*
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AdvancedElement
import com.workduck.models.EntityOperationType
import com.workduck.models.EntityServiceCreateResponse
import com.workduck.models.entityServiceResponses.MultipleEntityPaginatedResponse
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

class HighlightService(
    private val objectMapper: ObjectMapper = Helper.objectMapper,

    private val nodeService : NodeService = NodeService()
){

    fun createHighlight(wdRequest: WDRequest, userID: String, userWorkspaceID: String) : String {
        val request = wdRequest as HighlightRequest
        // this contains the nodeID to which smartCapture should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(request.nodeNamespaceMap, userID, userWorkspaceID)
        val highlight: AdvancedElement = request.data
        populateHighlightMetadata(highlight, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)
        val highlightID = invokeCreateHighlightLambda(highlight, nodeWorkspaceMap.workspaceID, userID).id
        val refBlock = EntityHelper.createEntityReferenceBlock(highlight.id, highlightID, Constants.ELEMENT_HIGHLIGHT)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))

        return highlightID
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

        return invokeGetCaptureLambda(workspaceID, userID, highlightID).data
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

        invokeDeleteCaptureLambda(workspaceID, userID, highlightID)
    }

    fun getAllHighlights(workspaceID: String, userID: String, lastKey: String?): MultipleEntityPaginatedResponse {
        return invokeGetAllHighlightsLambda(workspaceID, userID, lastKey)
    }

    fun getMultipleHighlights(wdRequest: WDRequest, userID: String, userWorkspaceID: String): MultipleEntityResponse {
        val request = wdRequest as GenericListRequest

        return invokeGetMultipleHighlightsLambda(request, userWorkspaceID, userID)

    }


    private fun getNodeIDWorkspaceID(nodeNamespaceMap: NodeNamespaceMap?, userID: String, userWorkspaceID: String) : NodeWorkspaceMap {

        return when(nodeNamespaceMap == null) {
            true -> { /* if no node,namespace is given we will add the smart capture to user's workspace in default place. */
                NodeWorkspaceMap(
                    nodeID = Constants.HIGHLIGHT_DEFAULT_NODE_ID,
                    workspaceID = Constants.WORKSPACE_ID
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


    private fun invokeCreateHighlightLambda(highlight: AdvancedElement, workspaceID: String, userID: String) : EntityServiceCreateResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.CREATE_HIGHLIGHT, HttpMethods.POST)
        val requestBody = objectMapper.writeValueAsString(EntityHelper.createEntityPayload(highlight))
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.HIGHLIGHT_LAMBDA, requestBody = requestBody)

        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)

    }

    private fun invokeGetCaptureLambda(workspaceID: String, userID: String, highlightID: String) : SingleEntityResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.GET_HIGHLIGHT, HttpMethods.GET)
        val pathParameters : Map<String, String> = mapOf("id" to highlightID)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.HIGHLIGHT_LAMBDA, pathParameters = pathParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)

    }

    private fun invokeDeleteCaptureLambda(workspaceID: String, userID: String, highlightID: String){
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.DELETE_HIGHLIGHT, HttpMethods.DELETE)
        val pathParameters : Map<String, String> = mapOf("id" to highlightID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.HIGHLIGHT_LAMBDA, pathParameters = pathParameters)
    }

    private fun invokeGetAllHighlightsLambda(workspaceID: String, userID: String, lastKey: String?)  : MultipleEntityPaginatedResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.GET_ALL_HIGHLIGHTS, HttpMethods.GET)
        val response = if (lastKey == null) LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.HIGHLIGHT_LAMBDA)
                        else LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.HIGHLIGHT_LAMBDA, queryStringParameters = mapOf("lastKey" to lastKey))
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        val multipleEntityResponseList: MultipleEntityPaginatedResponse= Helper.objectMapper.readValue(jsonBody, object : TypeReference<MultipleEntityPaginatedResponse>() {})
        return MultipleEntityPaginatedResponse(Items = multipleEntityResponseList.Items, lastKey = multipleEntityResponseList.lastKey)
    }

    private fun invokeGetMultipleHighlightsLambda(genericListRequest: GenericListRequest, workspaceID: String, userID: String)  : MultipleEntityResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.GET_MULTIPLE_HIGHLIGHTS, HttpMethods.POST)
        val requestBody = objectMapper.writeValueAsString(genericListRequest)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.HIGHLIGHT_LAMBDA, requestBody = requestBody)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        val singleEntityResponseList: List<SingleEntityResponse> = Helper.objectMapper.readValue(jsonBody, object : TypeReference<List<SingleEntityResponse>>() {})
        return MultipleEntityResponse(entities = singleEntityResponseList)
    }


    private fun populateHighlightMetadata(highlight: AdvancedElement, userID: String, createdAt : Long?, createdBy : String?){
        highlight.createdBy = createdBy
        highlight.createdAt = createdAt
        highlight.lastEditedBy = userID
        highlight.updatedAt = createdAt?: Constants.getCurrentTime()

    }

}
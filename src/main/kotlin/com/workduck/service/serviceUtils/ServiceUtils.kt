package com.workduck.service.serviceUtils

import com.amazonaws.services.lambda.model.InvocationType
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.NodeNamespaceMap
import com.serverless.models.requests.NodeWorkspaceMap
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
import com.workduck.service.NodeService
import com.workduck.utils.EntityHelper
import com.workduck.utils.Helper
import com.workduck.utils.LambdaHelper
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

object ServiceUtils {
    private val objectMapper: ObjectMapper = Helper.objectMapper

    fun invokeCreateOrUpdateEntityLambda(
        entity: AdvancedElement,
        workspaceID: String,
        userID: String,
        functionName: String,
        routePath: String,
        httpMethod: String,
        entityID: String? = null
    ) : EntityServiceCreateResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val requestBody = if(entityID == null) objectMapper.writeValueAsString(EntityHelper.createEntityPayload(entity))
                            else objectMapper.writeValueAsString(EntityHelper.updateEntityPayload(entityID, entity))
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, requestBody = requestBody)

        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)
    }

    fun invokeGetEntityLambda(
        workspaceID: String,
        userID: String,
        entityID: String,
        functionName: String,
        routePath: String,
        httpMethod: String
    ): SingleEntityResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val pathParameters : Map<String, String> = mapOf("id" to entityID)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, pathParameters = pathParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)
    }

    fun invokeDeleteEntityLambda(
        workspaceID: String,
        userID: String,
        entityID: String,
        functionName: String,
        routePath: String,
        httpMethod: String
    ){
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val pathParameters : Map<String, String> = mapOf("id" to entityID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, pathParameters = pathParameters)
    }

    fun invokeGetAllEntityLambda(
        workspaceID: String,
        userID: String,
        functionName: String,
        routePath: String,
        httpMethod: String,
        lastKey: String? = null
    )  : MultipleEntityPaginatedResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val response = if (lastKey == null) LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName)
                    else LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, queryStringParameters = mapOf("lastKey" to lastKey))
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        val multipleEntityResponseList: MultipleEntityPaginatedResponse = Helper.objectMapper.readValue(jsonBody, object : TypeReference<MultipleEntityPaginatedResponse>() {})
        return MultipleEntityPaginatedResponse(Items = multipleEntityResponseList.Items, lastKey = multipleEntityResponseList.lastKey)
    }

    fun invokeGetAllEntitiesByIDSLambda(
            genericListRequest: GenericListRequest,
            workspaceID: String,
            userID: String,
            functionName: String,
            routePath: String,
            httpMethod: String
    )  : MultipleEntityResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val requestBody = objectMapper.writeValueAsString(genericListRequest)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, requestBody = requestBody)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        val singleEntityResponseList: List<SingleEntityResponse> = Helper.objectMapper.readValue(jsonBody, object : TypeReference<List<SingleEntityResponse>>() {})
        return MultipleEntityResponse(entities = singleEntityResponseList)
    }

    fun invokeUpdateEntityLambda(
            entityID: String,
            entity: AdvancedElement,
            workspaceID: String,
            userID: String,
            functionName: String,
            routePath: String,
            httpMethod: String
    ){
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val requestBody = objectMapper.writeValueAsString(EntityHelper.createEntityPayload(entity))
        val pathParameters : Map<String, String> = mapOf("id" to entityID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, requestBody = requestBody, pathParameters = pathParameters)
    }

    fun invokeGetEntitiesWithFilterLambda(
            workspaceID: String,
            userID: String,
            filterType: String,
            filterValue: String,
            functionName: String,
            routePath: String,
            httpMethod: String,
            lastKey: String? = null
    )  : MultipleEntityResponse{
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val queryStringParameters : Map<String, String> = if (lastKey == null) mapOf("filterType" to filterType, "filterValue" to filterValue) 
                                                            else mapOf("filterType" to filterType, "filterValue" to filterValue, "lastKey" to lastKey)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, queryStringParameters = queryStringParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        val singleEntityResponseList: List<SingleEntityResponse> = Helper.objectMapper.readValue(jsonBody, object : TypeReference<List<SingleEntityResponse>>() {})
        return MultipleEntityResponse(entities = singleEntityResponseList)
    }

    fun populateEntityMetadata(entity: AdvancedElement, userID: String, createdAt : Long?, createdBy : String?){
        entity.createdBy = createdBy
        entity.createdAt = createdAt
        entity.lastEditedBy = userID
        entity.updatedAt = createdAt?: Constants.getCurrentTime()

    }

    fun getNodeIDWorkspaceID(nodeService: NodeService,nodeNamespaceMap: NodeNamespaceMap?, userID: String, userWorkspaceID: String) : NodeWorkspaceMap {
        return when(nodeNamespaceMap == null) {
            true -> { /* if no node,namespace is given we will add the highlight to user's workspace in default place. */
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
}
package com.workduck.service.serviceUtils

import com.amazonaws.services.lambda.model.InvocationType
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.NodeNamespaceMap
import com.serverless.models.requests.NodeWorkspaceMap
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AdvancedElement
import com.workduck.models.EntityOperationType
import com.workduck.models.EntityServiceCreateResponse
import com.workduck.models.externalRequests.ExternalRequestHeader
import com.workduck.models.externalRequests.RequestContext
import com.workduck.service.NodeService
import com.workduck.utils.EntityHelper
import com.workduck.utils.Helper
import com.workduck.utils.LambdaHelper

object ServiceUtils {

    fun invokeCreateOrUpdateEntityLambda(
        entity: AdvancedElement,
        workspaceID: String,
        userID: String,
        functionName: String,
        routePath: String,
        httpMethod: String,
        entityID: String? = null,
    ) : EntityServiceCreateResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val requestBody = if(entityID == null) Helper.objectMapper.writeValueAsString(EntityHelper.createEntityPayload(entity))
                            else Helper.objectMapper.writeValueAsString(EntityHelper.updateEntityPayload(entityID, entity))
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, requestBody = requestBody)

        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)
    }

    fun invokeCreateInstanceEntityLambda(
        entityID: String,
        workspaceID: String,
        userID: String,
        functionName: String,
        routePath: String,
        httpMethod: String,
        entity: AdvancedElement? = null
    ) : EntityServiceCreateResponse {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val requestBody =  if(entity != null)Helper.objectMapper.writeValueAsString(EntityHelper.createEntityPayload(entity)) else null
        val queryStringParameters : Map<String, String> = mapOf("parentID" to entityID)
        val response = if(requestBody != null) LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, queryStringParameters = queryStringParameters, requestBody = requestBody)
                        else LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, queryStringParameters = queryStringParameters)
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
    ): String{
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val pathParameters : Map<String, String> = mapOf("id" to entityID)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, pathParameters = pathParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return jsonBody
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
    )  : String {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val response = if (lastKey == null) LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName)
                    else LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, queryStringParameters = mapOf("lastKey" to lastKey))
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return jsonBody
    }

    fun invokeGetAllEntityInstancesByIDLambda(
        entityID: String,
        workspaceID: String,
        userID: String,
        functionName: String,
        routePath: String,
        httpMethod: String
    )  : String {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val pathParameters : Map<String, String> = mapOf("id" to entityID)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, pathParameters = pathParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return jsonBody
    }

    fun invokeGetAllEntitiesByIDSLambda(
            genericListRequest: GenericListRequest,
            workspaceID: String,
            userID: String,
            functionName: String,
            routePath: String,
            httpMethod: String
    )  : String {
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val requestBody = Helper.objectMapper.writeValueAsString(genericListRequest)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, requestBody = requestBody)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return jsonBody
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
        val requestBody = Helper.objectMapper.writeValueAsString(EntityHelper.createEntityPayload(entity))
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
    )  : String{
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(routePath, httpMethod)
        val queryStringParameters : Map<String, String> = if (lastKey == null) mapOf("filterType" to filterType, "filterValue" to filterValue) 
                                                            else mapOf("filterType" to filterType, "filterValue" to filterValue, "lastKey" to lastKey)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, functionName, queryStringParameters = queryStringParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return jsonBody
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
}
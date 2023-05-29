package com.workduck.service

import com.fasterxml.jackson.core.type.TypeReference
import com.serverless.models.requests.WDRequest
import com.workduck.models.AdvancedElement
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeDeleteEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.getNodeIDWorkspaceID
import com.workduck.service.serviceUtils.ServiceUtils.populateEntityMetadata
import com.workduck.service.serviceUtils.ServiceUtils.invokeCreateOrUpdateEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeUpdateEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeCreateInstanceEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntitiesByIDSLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntityLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetAllEntityInstancesByIDLambda
import com.workduck.service.serviceUtils.ServiceUtils.invokeGetEntitiesWithFilterLambda
import com.serverless.models.requests.EntityTypeRequest
import com.serverless.models.requests.HighlightInstanceRequest
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.MoveEntityRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.EntityOperationType
import com.workduck.models.Node
import com.workduck.models.entityServiceResponses.MultipleEntityPaginatedResponse
import com.workduck.models.entityServiceResponses.SingleEntityResponse
import com.workduck.utils.EntityHelper
import com.workduck.utils.Helper

abstract class EntityService(
    private val nodeService: NodeService = NodeService()
) {

    protected fun createOrUpdateEntity(
        wdRequest: WDRequest,
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        elementType: String,
        entityID: String?
    ): String? {
        val request = wdRequest as EntityTypeRequest
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val element: AdvancedElement = request.data!!
        populateEntityMetadata(element, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)
        if(entityID == null) {
            val elementID = invokeCreateOrUpdateEntityLambda(
                element,
                nodeWorkspaceMap.workspaceID,
                userID,
                lambdaFunctionName,
                routePath,
                httpMethod
            ).id
            val refBlock = EntityHelper.createEntityReferenceBlock(element.id, elementID, elementType)
            nodeService.appendEntityBlocks(
                nodeWorkspaceMap.nodeID,
                nodeWorkspaceMap.workspaceID,
                userID,
                listOf(refBlock)
            )

            return elementID
        }
        else {
            invokeCreateOrUpdateEntityLambda(
                element,
                nodeWorkspaceMap.workspaceID,
                userID,
                lambdaFunctionName,
                routePath,
                httpMethod,
                entityID
            ).id
            return null
        }
    }

    protected fun createEntityInstance(
        wdRequest: WDRequest,
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        elementType: String,
        parentID: String
    ): String {
        val request = wdRequest as HighlightInstanceRequest
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val elementID = invokeCreateInstanceEntityLambda(
            parentID,
            nodeWorkspaceMap.workspaceID,
            userID,
            lambdaFunctionName,
            routePath,
            httpMethod,
            request.data
        ).id
        val refBlock = EntityHelper.createEntityReferenceBlock(Constants.DEFAULT_VALUE, elementID, elementType)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))

        return elementID
    }

    protected fun updateEntity(
        wdRequest: WDRequest,
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        entityID: String
    ) {
        val request = wdRequest as EntityTypeRequest
        val nodeWorkspaceMap =
            getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val element: AdvancedElement = request.data!!
        populateEntityMetadata(element, userID, createdAt = null, createdBy = null)
        invokeUpdateEntityLambda(
            entityID,
            element,
            nodeWorkspaceMap.workspaceID,
            userID,
            lambdaFunctionName,
            routePath,
            httpMethod
        )
    }

    protected fun getEntity(
        userID: String,
        userWorkspaceID: String,
        nodeID: String,
        namespaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        entityID: String
    ): AdvancedElement? {
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
                entityID,
                lambdaFunctionName,
                routePath,
                httpMethod
            ),
            AdvancedElement::class.java
        )
    }

    protected fun deleteEntity(
        userID: String,
        userWorkspaceID: String,
        nodeID: String,
        namespaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        entityID: String
    ) {
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
            entityID,
            lambdaFunctionName,
            routePath,
            httpMethod
        )
    }

    protected fun moveEntity(
        request: MoveEntityRequest,
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        defaultNodeID: String,
        entityID: String
    ){
        // this map contains the nodeID to which entity should be moved and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)

        //TODO(ask directly for blockID from entity service)

        val blockID = Helper.objectMapper.readValue(
            invokeGetEntityLambda(
                userWorkspaceID,
                userID,
                entityID,
                lambdaFunctionName,
                routePath,
                httpMethod),
            SingleEntityResponse::class.java).data.id

        val sourceNodeWithBlockAndDataOrder: Node = nodeService.nodeRepository.getNodeWithBlockAndDataOrder(Constants.SMART_CAPTURE_DEFAULT_NODE_ID, blockID, userWorkspaceID).let{ node ->
            require(node != null) { Messages.INVALID_NODE_ID }
            require(node.data?.get(0) != null ) { Messages.INVALID_BLOCK_ID }
            check(!node.dataOrder.isNullOrEmpty()) {Messages.INVALID_NODE_STATE}
            node
        }

        sourceNodeWithBlockAndDataOrder.dataOrder!!.let { dataOrder ->
            nodeService.nodeRepository.moveBlock(sourceNodeWithBlockAndDataOrder.data!![0], userWorkspaceID, defaultNodeID, nodeWorkspaceMap.workspaceID, nodeWorkspaceMap.nodeID, dataOrder)
        }
    }

    protected fun getAllEntitiesForFilter(
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        filterType: String,
        filterValue: String,
        lastKey: String?
    ): MultipleEntityPaginatedResponse<AdvancedElement>{
        return Helper.objectMapper.readValue(
            invokeGetEntitiesWithFilterLambda(
                userWorkspaceID,
                userID,
                filterType,
                filterValue,
                lambdaFunctionName,
                routePath,
                httpMethod,
                lastKey
            ), object : TypeReference<MultipleEntityPaginatedResponse<AdvancedElement>>() {})
    }

    protected fun getAllEntityInstances(
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        parentID: String
    ): MultipleEntityPaginatedResponse<AdvancedElement> {
        return Helper.objectMapper.readValue(
            invokeGetAllEntityInstancesByIDLambda(
                parentID,
                workspaceID =  userWorkspaceID,
                userID,
                lambdaFunctionName,
                routePath,
                httpMethod
            ), object : TypeReference<MultipleEntityPaginatedResponse<AdvancedElement>>() {})
    }

    protected fun getAllEntitiesForWorkspace(
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        lastKey: String?
    ): MultipleEntityPaginatedResponse<AdvancedElement> {
        return Helper.objectMapper.readValue(
            invokeGetAllEntityLambda(
                userWorkspaceID,
                userID,
                lambdaFunctionName,
                routePath,
                httpMethod,
                lastKey
            ), object : TypeReference<MultipleEntityPaginatedResponse<AdvancedElement>>() {})
    }

    protected fun getAllEntitiesByIDs(
        wdRequest: WDRequest,
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String
    ): MultipleEntityPaginatedResponse<AdvancedElement> {
        val request = wdRequest as GenericListRequest

        return Helper.objectMapper.readValue(
            invokeGetAllEntitiesByIDSLambda(
                request,
                userWorkspaceID,
                userID,
                lambdaFunctionName,
                routePath,
                httpMethod
            ), object : TypeReference<MultipleEntityPaginatedResponse<AdvancedElement>>() {})
    }
}

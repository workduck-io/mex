package com.workduck.service

import com.serverless.models.requests.WDRequest
import com.workduck.models.AdvancedElement
import com.workduck.service.serviceUtils.ServiceUtils
import com.serverless.models.requests.EntityTypeRequest
import com.serverless.utils.Constants
import com.workduck.utils.EntityHelper

abstract class EntityService(
    private val nodeService: NodeService = NodeService()
) {

    protected fun createEntity(
        wdRequest: WDRequest,
        userID: String,
        userWorkspaceID: String,
        lambdaFunctionName: String,
        routePath: String,
        httpMethod: String,
        elementType: String
    ): String {
        val request = wdRequest as EntityTypeRequest
        val nodeWorkspaceMap = ServiceUtils.getNodeIDWorkspaceID(nodeService, request.nodeNamespaceMap, userID, userWorkspaceID)
        val element: AdvancedElement = request.data
        ServiceUtils.populateEntityMetadata(element, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)
        val elementID = ServiceUtils.invokeCreateOrUpdateEntityLambda(
            element,
            nodeWorkspaceMap.workspaceID,
            userID,
            lambdaFunctionName,
            routePath,
            httpMethod
        ).id
        val refBlock = EntityHelper.createEntityReferenceBlock(element.id, elementID, elementType)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))

        return elementID
    }

    // Other common methods can be added here as needed

}

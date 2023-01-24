package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService

class GetHierarchyStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val getMetadata = input.queryStringParameters?.let {
            it["getMetadata"]?.toBoolean()
        } ?: false

        return when (getMetadata) {
            true -> ApiResponseHelper.generateStandardResponse(namespaceService.getNodeHierarchyOfWorkspaceWithMetaData(input.headers.workspaceID, input.tokenBody.userID), Messages.ERROR_GETTING_HIERARCHY)
            false -> ApiResponseHelper.generateStandardResponse(namespaceService.getNodeHierarchyOfWorkspace(input.headers.workspaceID, input.tokenBody.userID), Messages.ERROR_GETTING_HIERARCHY)

        }

    }
}
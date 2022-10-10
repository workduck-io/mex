package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService

class GetAllSharedUsersStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        return input.pathParameters?.id?.let { namespaceID ->
            namespaceService.getAllSharedUsersOfNamespace(input.headers.workspaceID, namespaceID, input.tokenBody.userID).let { userIDToAccessMap ->
                ApiResponseHelper.generateStandardResponse(userIDToAccessMap, Messages.ERROR_GETTING_RECORDS)
            }
        }!! /* since path gets matched, nodeID will always be not null */

    }
}
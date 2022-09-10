package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.UserStarService

class GetStarsOfNamespaceStrategy : UserStarStrategy {
        override fun apply(input: Input, userStarService: UserStarService): ApiGatewayResponse {

            input.pathParameters!!.id!!.let { namespaceID ->
                val nodeIDList: List<String> = userStarService.getAllStarredNodesInNamespace(input.tokenBody.userID, input.headers.workspaceID, namespaceID)
                return ApiResponseHelper.generateStandardResponse(nodeIDList as Any, Messages.ERROR_GETTING_STARRED)
            } /* id cannot be null since path has been matched */
        }
}

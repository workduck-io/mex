package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.IdentifierHelper
import com.serverless.utils.Messages
import com.serverless.utils.NamespaceHelper
import com.serverless.utils.withNotFoundException
import com.workduck.models.Identifier
import com.workduck.service.NamespaceService

class DeleteNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        return input.pathParameters?.id?.let { namespaceID ->
            namespaceService.deleteNamespace(namespaceID, input.headers.workspaceID, input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_NAMESPACE)
            }
        }!!

    }
}

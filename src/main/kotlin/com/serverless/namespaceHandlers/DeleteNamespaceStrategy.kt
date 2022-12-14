package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.Constants
import com.serverless.utils.IdentifierHelper
import com.serverless.utils.Messages
import com.serverless.utils.NamespaceHelper
import com.serverless.utils.isValidID
import com.serverless.utils.withNotFoundException
import com.workduck.models.Identifier
import com.workduck.service.NamespaceService

class DeleteNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val successorNamespaceID = input.queryStringParameters?.let{
            it["successorNamespaceID"]?.let{ namespaceID ->
                require(namespaceID.isValidID(Constants.NAMESPACE_ID_PREFIX)) { Messages.INVALID_NAMESPACE_ID }
                namespaceID
            }
        }

        return input.pathParameters?.id?.let { namespaceID ->
            namespaceService.deleteNamespace(namespaceID, input.headers.workspaceID, successorNamespaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_NAMESPACE)
            }
        }!!

    }
}

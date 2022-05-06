package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.IdentifierHelper
import com.serverless.utils.Messages
import com.workduck.models.Identifier
import com.workduck.service.NamespaceService

class DeleteNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val namespaceID = input.pathParameters?.id

        return if (namespaceID != null) {
            val identifier: Identifier? = namespaceService.deleteNamespace(namespaceID)

            val identifierResponse: Response?  = IdentifierHelper.convertIdentifierToIdentifierResponse(identifier)

            ApiResponseHelper.generateStandardResponse(identifierResponse, Messages.ERROR_DELETING_NAMESPACE)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_DELETING_NAMESPACE)
        }
    }
}

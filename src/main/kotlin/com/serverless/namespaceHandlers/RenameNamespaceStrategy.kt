package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService

class RenameNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        return input.pathParameters!!.id!!.let {  namespaceID ->
            input.payload?.let { namespaceRequest ->
                namespaceService.renameNamespace(namespaceRequest, input.headers.workspaceID, namespaceID).let {
                    ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_NAMESPACE)
                }
            } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)
        } /* id cannot be null since path has been matched */

    }
}

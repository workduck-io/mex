package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.requests.SuccessorNamespaceRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Messages
import com.serverless.utils.isValidNamespaceID
import com.workduck.service.NamespaceService
import java.lang.IllegalArgumentException

class DeleteNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val successorNamespaceID = input.payload?.getSuccessorNamespaceID()
        return input.pathParameters?.id?.let { namespaceID ->
            namespaceService.deleteNamespace(namespaceID, input.headers.workspaceID, successorNamespaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_NAMESPACE)
            }
        }!!

    }
}


private fun WDRequest.getSuccessorNamespaceID():String =
    (this as SuccessorNamespaceRequest).let { it ->
        it.successorNamespaceID?.also { namespaceId ->
            namespaceId.also { it.checkForValidNamespace() }
        } ?: throw IllegalArgumentException( Messages.ERROR_NAMESPACE_PERMISSION)
    }


/*
Could be moved to generic extensions, on need basis
 */
private fun String?.checkForValidNamespace() = this?.let { namespaceID ->
    require(namespaceID.isValidNamespaceID()) { Messages.INVALID_NAMESPACE_ID }
}

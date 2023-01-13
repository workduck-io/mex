package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.isValidNamespaceID
import com.workduck.service.NamespaceService
import java.lang.IllegalArgumentException

class DeleteNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val successorNamespaceID = input.getSuccessorNamespaceID()
        return input.pathParameters?.id?.let { namespaceID ->
            namespaceService.deleteNamespace(namespaceID, input.headers.workspaceID, successorNamespaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_NAMESPACE)
            }
        }!!

    }
}



private fun Input.getSuccessorNamespaceID():String? = this.queryStringParameters?.let { map ->
   map["successorNamespaceID"]?.also { namespaceId ->
       namespaceId.also { it.checkForValidNamespace() }
   } ?: throw IllegalArgumentException( Messages.ERROR_NAMESPACE_PERMISSION)
}

/*
Could be moved to generic extensions, on need basis
 */
private fun String?.checkForValidNamespace() = this?.let { namespaceID ->
    require(namespaceID.isValidNamespaceID()) { Messages.INVALID_NAMESPACE_ID }
}

package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService

class GetAllNamespaceDataV2Strategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {

        val getOnlyShared = input.queryStringParameters?.get("onlyShared")?.let {
            it.toBoolean()
        } ?: false

        val getOnlyWorkspace = input.queryStringParameters?.get("onlyWorkspace")?.let {
            it.toBoolean()
        } ?: false


        if(getOnlyShared && getOnlyWorkspace){
            return namespaceService.getAllNamespaceMetadata(input.headers.workspaceID, input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NAMESPACES)
            }
        } else if(getOnlyShared){
            return namespaceService.getAllSharedNamespacesWithUser(input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NAMESPACES)
            }
        } else if(getOnlyWorkspace) {
            return namespaceService.getAllNamespacesOfWorkspace(input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NAMESPACES)
            }

        } else {
            return namespaceService.getAllNamespaceMetadata(input.headers.workspaceID, input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NAMESPACES)
            }
        }

    }
}

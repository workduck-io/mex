package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getNamespaceIDFromQueryParam
import com.serverless.utils.extensions.getNodeIDFromQueryParam
import com.workduck.service.SmartCaptureService

class GetSmartCaptureStrategy: SmartCaptureStrategy {
    override fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse {

        val nodeID = input.getNodeIDFromQueryParam() ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
        val namespaceID = input.getNamespaceIDFromQueryParam() ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)

        return input.pathParameters!!.id!!.let { captureID ->
            smartCaptureService.getSmartCapture(captureID, nodeID, namespaceID, input.headers.workspaceID, input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_SMART_CAPTURE)
            }
        } /* since the routeKey would be matched, can't be null */
    }
}
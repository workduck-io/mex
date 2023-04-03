package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getNamespaceIDFromPathParam
import com.workduck.service.SmartCaptureService

class UpdateSmartCaptureStrategy : SmartCaptureStrategy {
    override fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse {

        val id = input.getNamespaceIDFromPathParam()

        return input.payload?.let { smartCaptureRequest ->
            smartCaptureService.updateSmartCapture(id, smartCaptureRequest, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_CREATING_SMART_CAPTURE)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }

}
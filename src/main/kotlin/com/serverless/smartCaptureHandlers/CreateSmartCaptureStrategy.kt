package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SmartCaptureHelper
import com.workduck.service.SmartCaptureService

class CreateSmartCaptureStrategy: SmartCaptureStrategy {
    override fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse {
        return input.payload?.let { smartCaptureRequest ->
            smartCaptureService.createSmartCapture(smartCaptureRequest, input.tokenBody.userID, input.headers.workspaceID, input.headers.bearerToken).let {
                ApiResponseHelper.generateStandardResponse(null,204, Messages.ERROR_CREATING_SMART_CAPTURE)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }

}
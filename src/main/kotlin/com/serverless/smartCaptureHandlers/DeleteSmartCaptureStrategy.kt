package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.SmartCaptureService

class DeleteSmartCaptureStrategy: SmartCaptureStrategy {
    override fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse {
        return input.pathParameters?.id?.let { captureID ->
            smartCaptureService.deleteSmartCapture(captureID, input.headers.workspaceID, input.headers.bearerToken).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_SMART_CAPTURE)
            }
        }!!
    }
}
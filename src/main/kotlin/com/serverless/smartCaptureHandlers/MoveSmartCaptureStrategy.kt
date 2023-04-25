package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.SmartCaptureService

/* this will be used only when moving from smart capture from default node to a given node */
class MoveSmartCaptureStrategy : SmartCaptureStrategy {
    override fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse {
        return input.payload?.let { smartCaptureRequest ->
            smartCaptureService.moveSmartCapture(smartCaptureRequest, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_MOVING_SMART_CAPTURE)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }
}
package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SmartCaptureHelper
import com.serverless.utils.SnippetHelper
import com.serverless.utils.withNotFoundException
import com.workduck.service.SmartCaptureService

class GetSmartCaptureStrategy: SmartCaptureStrategy {
    override fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse {
        return input.pathParameters?.id?.let { captureID ->
            smartCaptureService.getSmartCapture(captureID, input.headers.workspaceID, input.headers.bearerToken).withNotFoundException().let {
                ApiResponseHelper.generateStandardResponse(SmartCaptureHelper.convertSmartCaptureToSmartCaptureResponse(it), Messages.ERROR_GETTING_SMART_CAPTURE)
            }
        }!!
    }
}
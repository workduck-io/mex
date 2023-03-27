package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SmartCaptureHelper
import com.serverless.utils.withNotFoundException
import com.workduck.service.SmartCaptureService

class GetAllSmartCapturesForUserStrategy: SmartCaptureStrategy {
    override fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse {
        return input.pathParameters?.configId?.let { configID ->
            smartCaptureService.getAllSmartCaptureForUser(configID, input.headers.workspaceID, input.headers.bearerToken).withNotFoundException().let {
                ApiResponseHelper.generateStandardResponse(SmartCaptureHelper.convertSmartCaptureToSmartCaptureArrayResponse(it), Messages.ERROR_DELETING_SMART_CAPTURE)
            }
        }!!
    }
}
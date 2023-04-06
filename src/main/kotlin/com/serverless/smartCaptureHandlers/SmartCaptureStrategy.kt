package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.SmartCaptureService


interface SmartCaptureStrategy {
    fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse
}

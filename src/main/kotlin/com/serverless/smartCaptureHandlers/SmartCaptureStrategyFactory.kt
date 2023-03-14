package com.serverless.smartCaptureHandlers

class SmartCaptureStrategyFactory {
    companion object {
        const val createSmartCapture = "POST /capture"

        private val smartCaptureRegistry: Map<String, SmartCaptureStrategy> = mapOf(
            createSmartCapture to CreateSmartCaptureStrategy()
        )

        fun getSmartCaptureStrategy(routeKey: String): SmartCaptureStrategy? {
            return smartCaptureRegistry[routeKey.replace("/v1", "")]
        }
    }
}
package com.serverless.smartCaptureHandlers

class SmartCaptureStrategyFactory {
    companion object {
        const val createSmartCapture = "POST /capture"
        const val getSmartCapture = "GET /capture/{id}"

        private val smartCaptureRegistry: Map<String, SmartCaptureStrategy> = mapOf(
            createSmartCapture to CreateSmartCaptureStrategy(),
            getSmartCapture to GetSmartCaptureStrategy()
        )

        fun getSmartCaptureStrategy(routeKey: String): SmartCaptureStrategy? {
            return smartCaptureRegistry[routeKey.replace("/v1", "")]
        }
    }
}
package com.serverless.smartCaptureHandlers

class SmartCaptureStrategyFactory {
    companion object {
        const val createSmartCapture = "POST /capture"
        const val getSmartCapture = "GET /capture/{id}"
        const val updateSmartCaptureStrategy = "PUT /capture/{id}"
        const val deleteSmartCapture = "DELETE /capture/{id}"
        const val getAllSmartCapturesForFilterStrategy = "GET /capture/filter"


        private val smartCaptureRegistry: Map<String, SmartCaptureStrategy> = mapOf(
            createSmartCapture to CreateSmartCaptureStrategy(),
            getSmartCapture to GetSmartCaptureStrategy(),
            getAllSmartCapturesForFilterStrategy to GetAllSmartCapturesForFilterStrategy(),
            deleteSmartCapture to DeleteSmartCaptureStrategy()
        )

        fun getSmartCaptureStrategy(routeKey: String): SmartCaptureStrategy? {
            return smartCaptureRegistry[routeKey.replace("/v1", "")]
        }
    }
}
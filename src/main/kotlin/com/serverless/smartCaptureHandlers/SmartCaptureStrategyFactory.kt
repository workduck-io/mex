package com.serverless.smartCaptureHandlers

class SmartCaptureStrategyFactory {
    companion object {
        const val createSmartCapture = "POST /capture"
        const val getSmartCapture = "GET /capture/{id}"
        const val updateSmartCaptureStrategy = "PUT /capture/{id}"
        const val deleteSmartCapture = "DELETE /capture/{id}"
        const val getAllSmartCapturesForFilterStrategy = "GET /capture/filter"
        const val moveSmartCaptureStrategy = "PATCH /capture/move"


        private val smartCaptureRegistry: Map<String, SmartCaptureStrategy> = mapOf(
            createSmartCapture to CreateSmartCaptureStrategy(),
            getSmartCapture to GetSmartCaptureStrategy(),
            getAllSmartCapturesForFilterStrategy to GetAllSmartCapturesForFilterStrategy(),
            updateSmartCaptureStrategy to UpdateSmartCaptureStrategy(),
            deleteSmartCapture to DeleteSmartCaptureStrategy(),
            moveSmartCaptureStrategy to MoveSmartCaptureStrategy()
        )

        fun getSmartCaptureStrategy(routeKey: String): SmartCaptureStrategy? {
            return smartCaptureRegistry[routeKey.replace("/v1", "")]
        }
    }
}
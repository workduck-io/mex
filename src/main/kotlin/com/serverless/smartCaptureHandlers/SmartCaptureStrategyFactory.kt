package com.serverless.smartCaptureHandlers

class SmartCaptureStrategyFactory {
    companion object {
        const val createSmartCapture = "POST /capture"
        const val getSmartCapture = "GET /capture/{id}"
        const val getAllSmartCaptureWithConfigId = "GET /config/{configId}/allcaptures"
        const val getAllSmartCapturesForUserStrategy = "GET /config/{configId}/usercaptures"
        const val deleteSmartCapture = "DELETE /capture/{id}"

        private val smartCaptureRegistry: Map<String, SmartCaptureStrategy> = mapOf(
            createSmartCapture to CreateSmartCaptureStrategy(),
            getSmartCapture to GetSmartCaptureStrategy(),
            getAllSmartCaptureWithConfigId to GetAllSmartCapturesWithConfigIdStrategy(),
            getAllSmartCapturesForUserStrategy to GetAllSmartCapturesForUserStrategy(),
            deleteSmartCapture to DeleteSmartCaptureStrategy()
        )

        fun getSmartCaptureStrategy(routeKey: String): SmartCaptureStrategy? {
            return smartCaptureRegistry[routeKey.replace("/v1", "")]
        }
    }
}
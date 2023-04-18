package com.serverless.highlightHandlers

class HighlightStrategyFactory {
    companion object {
        const val createHighlight = "POST /highlight"
        const val getHighlight = "GET /highlight/{id}"
        const val getMultipleHighlight = "POST /highlight/multiple"
        const val deleteHighlight = "DELETE /highlight/{id}"
        const val getAllHighlights = "GET /highlight/all"


        private val highlightRegistry: Map<String, HighlightStrategy> = mapOf(
            createHighlight to CreateHighlightStrategy(),
            getHighlight to GetHighlightStrategy(),
            deleteHighlight to DeleteHighlightStrategy(),
            getAllHighlights to GetAllHighlightsStrategy(),
            getMultipleHighlight to GetMultipleHighlightsStrategy()
        )

        fun getHighlightStrategy(routeKey: String): HighlightStrategy? {
            return highlightRegistry[routeKey.replace("/v1", "")]
        }
    }
}
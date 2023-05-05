package com.serverless.highlightHandlers

class HighlightStrategyFactory {
    companion object {
        const val createHighlight = "POST /highlight"
        const val updateHighlight = "PUT /highlight/{id}"
        const val getHighlight = "GET /highlight/{id}"
        const val getMultipleHighlight = "POST /highlight/ids"
        const val deleteHighlight = "DELETE /highlight/{id}"
        const val getAllHighlights = "GET /highlight/all"
        const val getAllHighlightInstances = "GET /highlight/instances/all/{id}"


        private val highlightRegistry: Map<String, HighlightStrategy> = mapOf(
            createHighlight to CreateHighlightStrategy(),
            updateHighlight to UpdateHighlightStrategy(),
            getHighlight to GetHighlightStrategy(),
            deleteHighlight to DeleteHighlightStrategy(),
            getAllHighlights to GetAllHighlightsStrategy(),
            getMultipleHighlight to GetAllHighlightsByIDsStrategy(),
            getAllHighlightInstances to GetAllHighlightInstancesStrategy()
        )

        fun getHighlightStrategy(routeKey: String): HighlightStrategy? {
            return highlightRegistry[routeKey.replace("/v1", "")]
        }
    }
}
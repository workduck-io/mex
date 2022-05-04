package com.serverless.internalTagHandlers

class InternalTagStrategyFactory {

    companion object {

        private const val addTagForNode = "POST /tag"

        private const val deleteTagForNode = "DELETE /tag"


        private val tagRegistry: Map<String, InternalTagStrategy> = mapOf(
                addTagForNode to AddTagForNodeStrategy(),
                deleteTagForNode to DeleteTagForNodeStrategy(),
        )

        fun getTagStrategy(routeKey: String): InternalTagStrategy? {
            return tagRegistry[routeKey]
        }
    }
}
package com.serverless.tagHandlers

class TagStrategyFactory {

    companion object {

        private const val getAllTagsOfWorkspace = "GET /tag/all"

        private const val getAllNodesByTag = "GET /tag/{tagName}/node"


        private val tagRegistry: Map<String, TagStrategy> = mapOf(
                getAllTagsOfWorkspace to GetAllTagsOfWorkspaceStrategy(),
                getAllNodesByTag to GetAllNodesByTagStrategy()
        )

        fun getTagStrategy(routeKey: String): TagStrategy? {
            return tagRegistry[routeKey.replace("/v1", "")]
        }
    }
}

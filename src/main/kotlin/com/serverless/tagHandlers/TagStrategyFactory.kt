package com.serverless.tagHandlers

class TagStrategyFactory {

    companion object {

        private const val addTagForNode = "POST /tag/{tagName}/{nodeID}"

        private const val deleteTagForNode = "POST /tag/{tagName}/{nodeID}"

        private const val getAllTagsOfWorkspace = "GET /tag/{workspaceID}"

        private const val getAllNodesByTag = "GET /tag/{tagName}/append"


        private val tagRegistry: Map<String, TagStrategy> = mapOf(
                addTagForNode to AddTagForNodeStrategy(),
                deleteTagForNode to DeleteTagForNodeStrategy(),
                getAllTagsOfWorkspace to GetAllTagsOfWorkspaceStrategy(),
                getAllNodesByTag to GetAllNodesByTagStrategy()
        )

        fun getTagStrategy(routeKey: String): TagStrategy? {
            return tagRegistry[routeKey]
        }
    }
}

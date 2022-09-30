package com.serverless.userBookmarkHandlers

class UserStarStrategyFactory {

    companion object {

        const val createStarObject = "POST /userStar/{id}"

        const val deleteStarObject = "DELETE /userStar/{id}"

        const val getStarsObject = "GET /userStar/all"

        const val getStarsOfNamespaceObject = "GET /userStar/namespace/{id}"

        const val createMultipleStarsObject = "POST /userStar/batch"

        const val deleteMultipleStarsObject = "DELETE /userStar/batch"

        private val userStarRegistry: Map<String, UserStarStrategy> = mapOf(
            createStarObject to CreateStarStrategy(),
            deleteStarObject to DeleteStarStrategy(),
            getStarsObject to GetStarsStrategy(),
            getStarsOfNamespaceObject to GetStarsOfNamespaceStrategy(),
            createMultipleStarsObject to CreateMultipleStarsStrategy(),
            deleteMultipleStarsObject to DeleteMultipleStarsStrategy()
        )

        fun getUserStarStrategy(routeKey: String): UserStarStrategy? {
            return userStarRegistry[routeKey.replace("/v1", "")]
        }
    }
}

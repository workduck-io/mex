package com.serverless.userBookmarkHandlers

class UserBookmarkStrategyFactory {

    companion object {

        const val createBookmarkObject = "POST /userBookmark/{userID}/{nodeID}"

        const val deleteBookmarkObject = "PATCH /userBookmark/{userID}/{nodeID}"

        const val getBookmarksObject = "GET /userBookmark/{userID}"

        const val createBookmarkInBatchObject = "POST /userBookmark/batch/{userID}/{ids}"

        const val deleteBookmarkInBatchObject = "PATCH /userBookmark/batch/{userID}/{ids}"

        private val userBookmarkRegistry: Map<String, UserBookmarkStrategy> = mapOf(
            createBookmarkObject to CreateBookmarkStrategy(),
            deleteBookmarkObject to DeleteBookmarkStrategy(),
            getBookmarksObject to GetBookmarksStrategy(),
            createBookmarkInBatchObject to CreateBookmarkInBatchStrategy(),
            deleteBookmarkInBatchObject to DeleteBookmarkInBatchStrategy()
        )

        fun getUserBookmarkStrategy(routeKey: String): UserBookmarkStrategy? {
            return userBookmarkRegistry[routeKey]
        }
    }
}

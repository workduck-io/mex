package com.serverless.userIdentifierMappingHandlers

class UserIdentifierMappingStrategyFactory {

    companion object {
        const val createUserIdentifierMappingObject = "POST /userIdentifierMappingRecord"

        const val getUserIdentifierMappingObject = "GET /userIdentifierMappingRecord/{userID}/{identifierID}"

        const val getUserRecordsObject = "GET /userRecords/{userID}"

        const val createBookmarkObject = "POST /userIdentifierMappingRecord/bookmark/{userID}/{nodeID}"

        const val deleteBookmarkObject = "PATCH /userIdentifierMappingRecord/bookmark/{userID}/{nodeID}"

        const val getBookmarksObject = "GET /userIdentifierMappingRecord/bookmark/{userID}"

        const val createBookmarkInBatchObject = "POST /userIdentifierMappingRecord/bookmark/batch/{userID}/{ids}"

        const val deleteBookmarkInBatchObject = "PATCH /userIdentifierMappingRecord/bookmark/batch/{userID}/{ids}"

        private val userIdentifierMappingRegistry: Map<String, UserIdentifierMappingStrategy> = mapOf(
            getUserIdentifierMappingObject to DeleteUserIdentifierMappingStrategy(),
            createUserIdentifierMappingObject to CreateUserIdentifierMappingStrategy(),
            getUserRecordsObject to GetUserRecordsStrategy(),
            createBookmarkObject to CreateBookmarkStrategy(),
            deleteBookmarkObject to DeleteBookmarkStrategy(),
            getBookmarksObject to GetBookmarksStrategy(),
            createBookmarkInBatchObject to CreateBookmarkInBatchStrategy(),
            deleteBookmarkInBatchObject to DeleteBookmarkInBatchStrategy()
        )

        fun getUserIdentifierMappingStrategy(routeKey: String): UserIdentifierMappingStrategy? {
            return userIdentifierMappingRegistry[routeKey]
        }
    }
}

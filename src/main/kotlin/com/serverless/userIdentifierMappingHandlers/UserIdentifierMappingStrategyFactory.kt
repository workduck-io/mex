package com.serverless.userIdentifierMappingHandlers

class UserIdentifierMappingStrategyFactory {

    companion object {
        const val createUserIdentifierMappingObject = "POST /userIdentifierMappingRecord"

        const val getUserIdentifierMappingObject = "GET /userIdentifierMappingRecord/{userID}/{identifierID}"

        const val getUserRecordsObject = "GET /userRecords/{userID}"

        const val createBookmarkObject = "POST /userIdentifierMappingRecord/bookmark/{userID}/{nodeID}"

        const val deleteBookmarkObject = "PATCH /userIdentifierMappingRecord/bookmark/{userID}/{nodeID}"

        const val getBookmarksObject = "GET /userIdentifierMappingRecord/bookmark/{userID}"

        private val userIdentifierMappingRegistry: Map<String, UserIdentifierMappingStrategy> = mapOf(
            getUserIdentifierMappingObject to DeleteUserIdentifierMappingStrategy(),
            createUserIdentifierMappingObject to CreateUserIdentifierMappingStrategy(),
            getUserRecordsObject to GetUserRecordsStrategy()
        )

        fun getUserIdentifierMappingStrategy(routeKey: String): UserIdentifierMappingStrategy? {
            return userIdentifierMappingRegistry[routeKey]
        }
    }
}

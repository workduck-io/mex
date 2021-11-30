package com.serverless.userPreferenceHandlers



class UserPreferenceStrategyFactory {

    companion object{
        const val getUserPreferenceRecord = "GET /userPreference/{id}/{preferenceType}"

        const val getAllUserPreferencesForUser = "GET /userPreference/all/{id}"

        const val createAndUpdateUserPreferenceRecord = "POST /userPreference"


        private val userPreferenceRegistry: Map<String, UserPreferenceStrategy> = mapOf(
                getUserPreferenceRecord to GetUserPreferenceRecordStrategy(),
                getAllUserPreferencesForUser to GetAllUserPreferencesForUserStrategy(),
                createAndUpdateUserPreferenceRecord to CreateAndUpdateUserPreferenceRecordStrategy()
        )

        fun getUserPreferenceStrategy(routeKey: String): UserPreferenceStrategy? {
            return userPreferenceRegistry[routeKey]
        }
    }
}
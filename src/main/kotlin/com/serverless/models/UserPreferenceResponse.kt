package com.serverless.models

data class UserPreferenceResponse(
    val userID: String,

    val preferenceType: String,

    val preferenceValue: String
) : Response

package com.serverless.models.responses

data class UserPreferenceResponse(
    val userID: String,

    val preferenceType: String,

    val preferenceValue: String
) : Response

package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("UserPreferenceRequest")
data class UserPreferenceRequest(
    @JsonProperty("userID")
    var userID: String = "",

    @JsonProperty("preferenceType")
    var preferenceType: String = "",

    @JsonProperty("preferenceValue")
    var preferenceValue: String = "",
) : WDRequest

package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.User

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("RegisterUserRequest")
data class RegisterUserRequest(
    @JsonProperty("user")
    val user: User,

    @JsonProperty("workspaceName")
    val workspaceName: String
) : WDRequest

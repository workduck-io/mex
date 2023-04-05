package com.workduck.models.externalRequests


data class RequestContext(
    val resourcePath: String,
    val httpMethod: String
)
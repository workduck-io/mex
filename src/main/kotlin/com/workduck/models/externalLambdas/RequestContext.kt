package com.workduck.models.externalLambdas


data class RequestContext(
    val resourcePath: String,
    val httpMethod: String
)
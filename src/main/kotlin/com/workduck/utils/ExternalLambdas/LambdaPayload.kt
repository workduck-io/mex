package com.workduck.utils.ExternalLambdas

import com.serverless.models.Header

data class LambdaPayload(
    val pathParameters: Map<String, String>? = null,
    val headers : Header,
    val body: String? = null,
    val httpMethod: String,
    val path: String,
    val requestContext: RequestContext,
    val queryStringParameters: Map<String, String>? = null,
    val routeKey: String
)

data class RequestContext(
    val resourcePath: String,
    val httpMethod: String
)

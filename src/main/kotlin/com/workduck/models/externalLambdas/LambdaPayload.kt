package com.workduck.models.externalLambdas


data class LambdaPayload(
    val headers : ExternalRequestHeader,
    val requestContext: RequestContext,
    val httpMethod: String = requestContext.httpMethod,
    val path: String = requestContext.resourcePath,
    val routeKey: String = "${requestContext.httpMethod} ${requestContext.resourcePath}",
    val queryStringParameters: Map<String, String>? = null,
    val body: String? = null,
    val pathParameters: Map<String, String>? = null,
)


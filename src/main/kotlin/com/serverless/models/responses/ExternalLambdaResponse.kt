package com.serverless.models.responses

data class ExternalLambdaResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, Any>
): Response
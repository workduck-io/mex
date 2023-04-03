package com.serverless.models.responses

data class ExternalResponse(
    val statusCode: Int,
    val body: String?,
    val headers: Map<String, Any>,
    val errorMessage: String?
): Response
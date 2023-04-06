package com.workduck.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.serverless.models.responses.ExternalResponse
import com.workduck.models.exceptions.WDServiceClientErrorException
import com.workduck.models.exceptions.WDServiceServerErrorException
import com.workduck.models.externalRequests.ExternalRequestHeader
import com.workduck.models.externalRequests.LambdaPayload
import com.workduck.models.externalRequests.RequestContext

object LambdaHelper {

    fun invokeLambda(
        header: ExternalRequestHeader,
        requestContext: RequestContext,
        invocationType: InvocationType,
        functionName: String,
        requestBody: String? = null,
        pathParameters: Map<String, String>? = null,
        queryStringParameters: Map<String, String>? = null
    ): ExternalResponse {

        val lambdaClient = AWSLambdaClient.builder()
            .withRegion(Regions.US_EAST_1)
            .build()

        val payload = LambdaPayload(
            body = requestBody,
            headers = header,
            requestContext = requestContext,
            pathParameters = pathParameters,
            queryStringParameters = queryStringParameters
        )

        val response = lambdaClient.invoke(
            InvokeRequest()
                .withFunctionName(functionName)
                .withInvocationType(invocationType)
                .withPayload(Helper.objectMapper.writeValueAsString(payload).trimIndent())
        ).payload.array()

        val externalResponse = Helper.objectMapper.readValue(String(response, Charsets.UTF_8), ExternalResponse::class.java)
        handlerError(externalResponse)

        return externalResponse
    }

    private fun handlerError(externalResponse: ExternalResponse) {

        if (externalResponse.statusCode in 400..599) {
            val errorMessage = extractErrorMessage(externalResponse.body) ?: "An error occurred."

            when (externalResponse.statusCode) {
                in 400..499 -> throw WDServiceClientErrorException(externalResponse.statusCode, errorMessage)
                else -> throw WDServiceServerErrorException(externalResponse.statusCode, errorMessage)
            }
        }
    }

    private fun extractErrorMessage(responseBody: String?): String? {
        return try {
            val jsonBody = Helper.objectMapper.readValue(responseBody, Map::class.java)
            jsonBody["message"] as? String
        } catch (e: Exception) {
            null
        }
    }
}

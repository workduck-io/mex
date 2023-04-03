package com.workduck.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.serverless.models.Header
import com.serverless.models.responses.ExternalResponse
import com.workduck.models.exceptions.WDServiceClientErrorException
import com.workduck.models.exceptions.WDServiceServerErrorException
import com.workduck.models.externalLambdas.ExternalRequestHeader
import com.workduck.models.externalLambdas.LambdaPayload
import com.workduck.models.externalLambdas.RequestContext

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
        externalResponse.errorMessage?.let { errorMessage ->
            when (externalResponse.statusCode) {
                in 400..499 -> throw WDServiceClientErrorException(externalResponse.statusCode, errorMessage)
                else -> throw WDServiceServerErrorException(externalResponse.statusCode, errorMessage)
            }
        }

        // for the unhandled exceptions by EntityService.
        if(externalResponse.statusCode == 500) throw WDServiceServerErrorException(externalResponse.statusCode, externalResponse.body ?: "")
    }
}

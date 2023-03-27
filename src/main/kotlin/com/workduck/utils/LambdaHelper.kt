package com.workduck.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.serverless.models.Header
import com.serverless.models.responses.ExternalLambdaResponse
import com.workduck.models.externalLambdas.LambdaPayload
import com.workduck.models.externalLambdas.RequestContext

object LambdaHelper {

    fun invokeLambda(
        header: Header,
        requestContext: RequestContext,
        invocationType: InvocationType,
        functionName: String,
        requestBody: String? = null,
        pathParameters: Map<String, String>? = null
    ): ExternalLambdaResponse {

        val lambdaClient = AWSLambdaClient.builder()
            .withRegion(Regions.US_EAST_1)
            .build()

        val payload = LambdaPayload(
            body = requestBody,
            headers = header,
            requestContext = requestContext,
            pathParameters = pathParameters
        )

        val response = lambdaClient.invoke(
            InvokeRequest()
                .withFunctionName(functionName)
                .withInvocationType(invocationType)
                .withPayload(Helper.objectMapper.writeValueAsString(payload).trimIndent())
        ).payload.array()

        return Helper.objectMapper.readValue(String(response, Charsets.UTF_8), ExternalLambdaResponse::class.java)
    }
}

package com.serverless.models

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiResponseHelper
import com.google.gson.Gson
import com.serverless.models.requests.WDRequest
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

@JsonIgnoreProperties(ignoreUnknown = true)
data class Input(
    val pathParameters: PathParameter?,
    val headers : Header,
    val body: String?,
    val bodyMap: Map<String, Any>?,
    val routeKey: String,
    val queryStringParameters: Map<String, String>?

) {
    // TODO(Figure out a way so that we can assign "body" WDRequest directly instead of using payload field)
    val payload: WDRequest? = body?.let { Gson().toJson(body).let{
        Helper.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true).readValue(it) }}

    val internalPayLoad : WDRequest? = bodyMap?.let {
        Helper.objectMapper.readValue(Helper.objectMapper.writeValueAsString(it))
    }


    val tokenBody: TokenBody = TokenBody.fromToken(headers.bearerToken) ?: throw UnauthorizedException("Unauthorized")

    companion object {
        fun fromMap(rawInput: Map<String, Any>): Input? {
            return try {
                Helper.objectMapper.convertValue(rawInput, Input::class.java)
            } catch (e : Exception){
                LOG.info(e)
                null
            }
        }

        private val LOG = LogManager.getLogger(Input::class.java)
    }
}

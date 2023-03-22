package com.serverless.models

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Messages
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

@JsonIgnoreProperties(ignoreUnknown = true)
data class Input(
    val pathParameters: PathParameter?,
    val headers : Header,
    val body: String?,
    val routeKey: String?,
    val queryStringParameters: Map<String, String>?,
    val httpMethod: String?,
    val resource: String?

) {

    val myRouteKey : String = when(!routeKey.isNullOrEmpty()){
        true -> routeKey
        false -> {
            require(!httpMethod.isNullOrEmpty() && !resource.isNullOrEmpty()) { "Invalid Request" }
            "$httpMethod $resource"
        }
    }

    val payload: WDRequest? = body?.let { Helper.objectMapper.readValue(it) }

    val tokenBody: TokenBody = TokenBody.fromToken(headers.bearerToken) ?: throw UnauthorizedException(Messages.UNAUTHORIZED)

    companion object {
        fun fromMap(rawInput: Map<String, Any>): Input? {
            return try {
                Helper.objectMapper.convertValue(rawInput, Input::class.java)
            } catch (e : Exception){
                LOG.debug(e)
                null
            }
        }

        private val LOG = LogManager.getLogger(Input::class.java)
    }
}
package com.serverless.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.WDRequest
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

@JsonIgnoreProperties(ignoreUnknown = true)
data class Input(
    val pathParameters: PathParameter?,
    val headers : Header,
    val body: String?,
    val routeKey: String,
    val queryStringParameters: Map<String, String>?

) {
    // TODO(Figure out a way so that we can assign "body" WDRequest directly instead of using payload field)
    val payload: WDRequest? = body?.let { Helper.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true).readValue(body) }

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

package com.serverless.internalTagHandlers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.WDRequest
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

data class TagInput(
        val routeKey :String,
        val workspaceID : String,
        val body: JsonNode,
) {

    val payload : WDRequest = Helper.objectMapper
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .readValue(body.toString())


    companion object {
        fun fromMap(rawInput: Map<String, Any>): TagInput? {
            return try {
                Helper.objectMapper.convertValue(rawInput, TagInput::class.java)
            } catch (e : Exception){
                LOG.info(e)
                null
            }
        }

        private val LOG = LogManager.getLogger(TagInput::class.java)
    }
}

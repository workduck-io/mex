package com.serverless.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.utils.Helper

@JsonIgnoreProperties(ignoreUnknown = true)
data class Input(
        val pathParameters : PathParameter?,
        val body: String?,
        val routeKey: String,
        val queryStringParameters : Map<String, String>?

){
    // TODO(Figure out a way so that we can assign "body" WDRequest directly instead of using payload field)
    val payload: WDRequest? = body?.let{ Helper.objectMapper.readValue(body) }

    companion object {
        fun fromMap(rawInput: Map<String, Any>) : Input =
                Helper.objectMapper.convertValue(rawInput, Input::class.java)
    }


}
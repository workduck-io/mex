package com.serverless.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.workduck.utils.Helper

@JsonIgnoreProperties(ignoreUnknown = true)
data class Input(
        val pathParameters : PathParameter?,
        val body: String?,
        val routeKey: String
){
    companion object {
        fun fromMap(rawInput: Map<String, Any>) : Input =
                Helper.objectMapper.convertValue(rawInput, Input::class.java)
    }

    //val httpMethod = this.routeKey.split(" ")[0];
}
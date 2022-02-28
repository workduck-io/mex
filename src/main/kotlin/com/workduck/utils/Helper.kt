package com.workduck.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.nodeHandlers.NodeHandler
import java.util.UUID

object Helper {

    val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    private fun uuidBase32(prefix: String): String? {
        return uuidBase32(UUID.randomUUID(), java.lang.StringBuilder(prefix)).toString()
    }

    private fun uuidBase32(uuid: UUID, builder: StringBuilder): StringBuilder? {
        Base32.encode(uuid.mostSignificantBits, builder)
        Base32.encode(uuid.leastSignificantBits, builder)
        return builder
    }

    fun generateId(prefix: String?): String {
        return uuidBase32(UUID.randomUUID(), StringBuilder(prefix)).toString()
    }

    fun isSourceWarmup(source : String?) : Boolean {
        return "serverless-plugin-warmup" == source
    }

    fun CharSequence.splitIgnoreEmpty(vararg delimiters: String): List<String> {
        return this.split(*delimiters).filter {
            it.isNotEmpty()
        }
    }
}

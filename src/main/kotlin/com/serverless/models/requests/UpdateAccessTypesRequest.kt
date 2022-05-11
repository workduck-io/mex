package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.serverless.utils.isMalformed
import com.workduck.converters.AccessTypeDeserializer
import com.workduck.converters.AccessTypeMapDeserializer
import com.workduck.models.AccessType


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("UpdateAccessTypesRequest")
data class UpdateAccessTypesRequest(

        val nodeID: String,

        @JsonDeserialize(converter = AccessTypeMapDeserializer::class)
        val userIDToAccessTypeMap: Map<String, AccessType> = mapOf(),

) : WDRequest {

    init {
        require(userIDToAccessTypeMap.isNotEmpty()) { "Need to provide accessMap" }

        require(!nodeID.isMalformed()) { "Invalid NodeID" }

    }
}
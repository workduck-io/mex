package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.serverless.utils.isMalformed
import com.workduck.converters.AccessTypeDeserializer
import com.workduck.models.AccessType
import com.workduck.models.ItemType

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SharedNodeRequest")
data class SharedNodeRequest(

    val nodeID: String,

    val userIDs: List<String> = listOf(),

    @JsonDeserialize(converter = AccessTypeDeserializer::class)
    val accessType: AccessType = AccessType.MANAGE

) : WDRequest {

    init {
        require(userIDs.isNotEmpty()) { "Need to provide userIDs" }

        require(!nodeID.isMalformed()) { "Invalid NodeID" }

    }
}

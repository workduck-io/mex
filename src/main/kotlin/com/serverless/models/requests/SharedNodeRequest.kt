package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidNodeID
import com.workduck.converters.AccessTypeDeserializer
import com.workduck.models.AccessType

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SharedNodeRequest")
data class SharedNodeRequest(

    val nodeID: String,

    val userIDs: List<String>,

    @JsonDeserialize(converter = AccessTypeDeserializer::class)
    val accessType: AccessType = AccessType.MANAGE

) : WDRequest {

    init {
        require(userIDs.isNotEmpty()) { "Need to provide userIDs" }

        require(nodeID.isValidNodeID()) { Messages.INVALID_NODE_ID }

    }
}

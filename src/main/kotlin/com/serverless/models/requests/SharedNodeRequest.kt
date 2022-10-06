package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
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

        require(nodeID.isValidID(Constants.NODE_ID_PREFIX)) { "Invalid NodeID" }

    }
}

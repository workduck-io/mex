package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
import com.workduck.converters.BlockMovementActionDeserializer
import com.workduck.models.BlockMovementAction

@JsonIgnoreProperties(ignoreUnknown = true)
class BlockMovementRequest(
    @JsonProperty("action")
    @JsonDeserialize(converter = BlockMovementActionDeserializer::class)
    val action: BlockMovementAction,

    @JsonProperty("blockID")
    val blockID: String,

    @JsonProperty("sourceNodeID")
    val sourceNodeID: String,

    @JsonProperty("sourceNamespaceID")
    val sourceNamespaceID: String,

    @JsonProperty("destinationNodeID")
    val destinationNodeID: String,

    @JsonProperty("destinationNamespaceID")
    val destinationNamespaceID: String

) : WDRequest {
    init {
        require(sourceNodeID.isValidID(Constants.NODE_ID_PREFIX) && destinationNodeID.isValidID(Constants.NODE_ID_PREFIX)) {
            "NodeID(s) invalid"
        }

        require(sourceNamespaceID.isValidID(Constants.NAMESPACE_ID_PREFIX) && destinationNamespaceID.isValidID(Constants.NAMESPACE_ID_PREFIX)) {
            "NamespaceID(s) invalid"
        }

        require(sourceNodeID != destinationNodeID) {
            "NodeIDs should be different"
        }

    }
}

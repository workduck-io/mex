package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.serverless.utils.extensions.isValidNamespaceID
import com.serverless.utils.extensions.isValidNodeID
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
        require(sourceNodeID.isValidNodeID() && destinationNodeID.isValidNodeID()) {
            "NodeID(s) invalid"
        }

        require(sourceNamespaceID.isValidNamespaceID() && destinationNamespaceID.isValidNamespaceID()) {
            "NamespaceID(s) invalid"
        }

        require(sourceNodeID != destinationNodeID) {
            "NodeIDs should be different"
        }

    }
}

package com.serverless.ddbStreamTriggers.publicnoteUpdateTrigger

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.fasterxml.jackson.annotation.JsonProperty

data class DDBStreamPayload(
    @JsonProperty("NewImage")
    val NewImage: Map<String, Any>?,

    @JsonProperty("OldImage")
    val OldImage: Map<String, Any>?,

    @JsonProperty("EventName")
    val EventName: String

) : DynamodbEvent()
package com.serverless.sqsNodeEventHandlers

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "Type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = DDBPayload::class, name = "DDBPayload"),
    JsonSubTypes.Type(value = S3Payload::class, name = "S3Payload")
)
sealed class SQSPayload

data class DDBPayload(
    @JsonProperty("NewImage")
    val NewImage: Map<String, Any>?,

    @JsonProperty("OldImage")
    val OldImage: Map<String, Any>?,

    @JsonProperty("EventName")
    val EventName: String
) : SQSPayload()

data class S3Payload(
    @JsonProperty("Key")
    val Key: String
) : SQSPayload()

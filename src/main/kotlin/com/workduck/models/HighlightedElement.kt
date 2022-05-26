package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.converters.HighlightMetadataConverter

@JsonTypeName("HighlightedElement")
data class HighlightedElement(

    @JsonProperty("highlightMetadata")
    @DynamoDBTypeConverted(converter = HighlightMetadataConverter::class)
    var highlightMetadata: HighlightMetadata? = null,

) : AdvancedElement()

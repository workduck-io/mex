package com.workduck.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes(
        JsonSubTypes.Type(value = HighlightMetadata::class, name = "highlight")
)
sealed class ElementMetadata



data class HighlightMetadata (
        var saveableRange: SaveableRange ?= null,

        var sourceUrl: String ?= null
) : ElementMetadata()

package com.workduck.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.workduck.utils.Helper

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = HighlightMetadata::class, name = "highlight")
)
sealed class ElementMetadata

data class HighlightMetadata(
    val id : String = Helper.generateNanoID(IdentifierType.HIGHLIGHT.name)
) : ElementMetadata()

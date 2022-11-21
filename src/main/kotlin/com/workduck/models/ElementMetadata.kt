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
    JsonSubTypes.Type(value = HighlightMetadata::class, name = "highlight"),
    JsonSubTypes.Type(value = HighlightMetadataV1::class, name = "highlightV1")
)
sealed class ElementMetadata


data class HighlightMetadata(
    val id : String = Helper.generateNanoID(IdentifierType.HIGHLIGHT.name)
) : ElementMetadata()

data class HighlightMetadataV1(
    val id : String = Helper.generateNanoID(IdentifierType.HIGHLIGHT.name)
) : ElementMetadata()

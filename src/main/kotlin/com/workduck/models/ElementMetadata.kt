package com.workduck.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.workduck.utils.Helper

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = HighlightMetadata::class, name = "highlight"),
    JsonSubTypes.Type(value = HighlightMetadataV1::class, name = "highlightV1"),
    JsonSubTypes.Type(value = SmartCaptureMetadata::class, name = "smartCapture")
)
sealed class ElementMetadata

@JsonIgnoreProperties(ignoreUnknown = true)
data class HighlightMetadata(
    var saveableRange: SaveableRange ? = null,
    var sourceUrl: String ? = null
) : ElementMetadata()


@JsonIgnoreProperties(ignoreUnknown = true)
data class HighlightMetadataV1(
    val id : String = Helper.generateNanoID(IdentifierType.HIGHLIGHT.name)
) : ElementMetadata()


@JsonIgnoreProperties(ignoreUnknown = true)
data class SmartCaptureMetadata(
    val page: String,
    val sourceUrl: String,
    val configID : String
) : ElementMetadata()

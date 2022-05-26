package com.workduck.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes(
        JsonSubTypes.Type(value = BlockILink::class, name = "blockILink"),
        JsonSubTypes.Type(value = NodeILink::class, name = "nodeILink"),
        JsonSubTypes.Type(value = WebLink::class, name = "webLink"),
)
sealed class LinkElement : AdvancedElement()


class BlockILink(
    val blockID: String
) : LinkElement()


class NodeILink(
    val workspaceID: String? = null,
    val nodeID: String? = null
) : LinkElement()


class WebLink(
    val url: String? = null
)

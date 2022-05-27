package com.workduck.models


sealed class LinkElement : AdvancedElement()


class BlockILink(
    var workspaceID: String? = null,
    var nodeID: String? = null,
    var blockID: String? = null
) : LinkElement()


class NodeILink(
    var workspaceID: String? = null,
    var nodeID: String? = null
) : LinkElement()


class WebLink(
    var url: String? = null
)

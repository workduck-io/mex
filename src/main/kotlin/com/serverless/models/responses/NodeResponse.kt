package com.serverless.models.responses

import com.workduck.models.AdvancedElement
import com.workduck.models.NodeMetadata

data class NodeResponse(
    val id: String? = null,

    val title: String,

    val data: List<AdvancedElement> ? = null,

    val lastEditedBy: String?,

    val createdBy: String?,

    override val createdAt: Long?,

    override val updatedAt: Long,

    val itemType: String = "Node",

    var tags: MutableList<String>?,

    var version: Int? = null,

    var namespaceID: String? = null,

    var workspaceID: String,

    var isBookmarked: Boolean? = null,

//    var saveableRange: SaveableRange? = null,
//
//    var sourceUrl: String? = null,

    var publicAccess: Boolean,

    var metadata: NodeMetadata?
) : Response, TimestampAdhereResponse

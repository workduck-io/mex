package com.serverless.models.responses

import com.workduck.models.AdvancedElement
import com.workduck.models.SaveableRange

data class NodeResponse(
    val id: String? = null,

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

    var publicAccess: Boolean
) : Response, TimestampAdhereResponse

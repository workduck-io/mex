package com.serverless.models.responses

import com.workduck.models.AdvancedElement

data class NodeResponse(
    val id: String? = null,

    val nodePath: String?= null,

    val data: List<AdvancedElement> ? = null,

    val lastEditedBy: String?,

    val createdBy: String?,

    override val createdAt: Long,

    override val updatedAt: Long,

    val itemType: String = "Node",

    var tags: MutableList<String>?,

    var version: Long? = null,

    var namespaceID: String? = null,

    var workspaceID: String? = null,

    var isBookmarked: Boolean? = null,

    var publicAccess: Boolean
) : Response, TimestampAdhereResponse

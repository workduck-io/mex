package com.serverless.models.responses

import com.workduck.models.AdvancedElement

data class NodeResponse(
    val id: String? = null,

    val nodePath: String? = null,

    override val data: List<AdvancedElement> ? = null,

    override val lastEditedBy: String?,

    override val createdBy: String?,

    override val createdAt: Long,

    override val updatedAt: Long,

    val itemType: String = "Node",

    var tags: MutableList<String>?,

    override var version: Long? = null,

    var namespaceID: String? = null,

    var workspaceID: String,

    var isBookmarked: Boolean? = null,

    var publicAccess: Boolean
) : Response, PageResponse, TimestampAdhereResponse

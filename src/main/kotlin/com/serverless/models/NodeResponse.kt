package com.serverless.models

import com.workduck.models.AdvancedElement

data class NodeResponse(
    val id: String? = null,

    val data: List<AdvancedElement> ? = null,

    val lastEditedBy: String?,

    val createdBy: String?,

    val createdAt: Long?,

    val updateAt: Long,

    val itemType: String = "Node",

    var version: Long? = null,

    var namespaceID: String? = null,

    var workspaceID: String? = null,

    var isBookmarked: Boolean? = null
) : Response

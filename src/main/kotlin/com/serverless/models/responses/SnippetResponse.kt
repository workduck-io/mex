package com.serverless.models.responses

import com.workduck.models.AdvancedElement

data class SnippetResponse(
    val id: String?,

    override val data: List<AdvancedElement>?,

    override val lastEditedBy: String?,

    override val createdBy: String?,

    override val createdAt: Long,

    override val updatedAt: Long,

    val itemType: String = "Snippet",

    override var version: Long?,

    override var namespaceID: String?,

    override var workspaceID: String,

    override var publicAccess: Boolean

) : Response, PageResponse, TimestampAdhereResponse

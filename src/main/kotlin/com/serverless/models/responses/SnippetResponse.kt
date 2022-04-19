package com.serverless.models.responses

import com.workduck.models.AdvancedElement

data class SnippetResponse(
    val id: String?,

    val data: List<AdvancedElement>?,

    val title: String,

    val lastEditedBy: String?,

    val createdBy: String?,

    override val createdAt: Long?,

    override val updatedAt: Long,

    var version: Int?,


) : Response, TimestampAdhereResponse

package com.serverless.models.responses

import com.workduck.models.AdvancedElement
import com.workduck.models.BlockElement
import com.workduck.models.PageMetadata

data class SmartCaptureResponse (
    val id: String?,

    val captureReference: List<BlockElement>?,

    val content: List<AdvancedElement>? = null,

    val title: String,

    val lastEditedBy: String?,

    val createdBy: String?,

    override val createdAt: Long?,

    override val updatedAt: Long,

    var version: Int?,

    var template: Boolean?,

    var metadata: PageMetadata?
) : Response, TimestampAdhereResponse

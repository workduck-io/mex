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


class CaptureEntity(){
    var captureId: String? = null
    var configId: String? = null
    var page: String? = null
    var source: String? = null
    var data: List<CaptureElements>? = null
    var workspaceId: String? = null
    var userId: String? = null
    var createdAt: Long? = null
    var modifiedAt: Long? = null
}

class CaptureElements(
    var id: String? = null,
    var label: String? = null,
    var value: String? = null,
    var properties: Map<String, Any>? = null,
)
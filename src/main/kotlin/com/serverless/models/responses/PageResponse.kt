package com.serverless.models.responses

import com.workduck.models.AdvancedElement

interface PageResponse {
    val data: List<AdvancedElement>?

    val lastEditedBy: String?

    val createdBy: String?

    val workspaceID: String

    var version: Long?

    val publicAccess: Boolean
}
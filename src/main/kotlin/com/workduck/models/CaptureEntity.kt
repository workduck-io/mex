package com.workduck.models

data class CaptureEntity(
    var captureID: String,

    var configID: String,

    var page: String,

    var source: String,

    var data: List<CaptureElements>,

    var workspaceID: String,

    var userID: String? = null,

    var createdAt: Long? = null,

    var modifiedAt: Long? = null,
)

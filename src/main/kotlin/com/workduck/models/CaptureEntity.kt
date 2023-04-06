package com.workduck.models

data class CaptureEntity(
    var captureID: String,

    var configID: String,

    var page: String?= null,

    var source: String?=null,

    var data: List<CaptureElements>?=null,

    var workspaceID: String?=null,

    var userID: String? = null,

    var createdAt: Long? = null,
)
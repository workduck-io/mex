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

class CaptureElements(
    var id: String,
    var label: String,
    var value: String,
    var properties: Map<String, Any>? = null,
)

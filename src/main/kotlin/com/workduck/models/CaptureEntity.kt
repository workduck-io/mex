package com.workduck.models

class CaptureEntity() {
    var captureId: String = ""
    var configId: String = ""
    var page: String = ""
    var source: String = ""
    var data: List<CaptureElements>? = null
    var workspaceId: String? = null
    var userId: String? = null
    var createdAt: Long? = null
    var modifiedAt: Long? = null
}

class CaptureElements(
    var id: String,
    var label: String,
    var value: String,
    var properties: Map<String, Any>? = null,
)
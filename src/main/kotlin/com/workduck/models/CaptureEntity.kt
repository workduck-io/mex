package com.workduck.models

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
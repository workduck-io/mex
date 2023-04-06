package com.workduck.models


data class CaptureElements(
    var id: String,

    var label: String,

    var value: String,

    var properties: Map<String, Any> = mapOf(),
)
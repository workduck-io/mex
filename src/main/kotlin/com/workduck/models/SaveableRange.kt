package com.workduck.models

data class SaveableRange(
    var startMeta: HighlightMeta,

    var endMeta: HighlightMeta,

    var text: String,

    var id: String,
)
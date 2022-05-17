package com.workduck.models

data class SaveableRange(
    var startMeta: HighlightPosition ?= null,

    var endMeta: HighlightPosition ?= null,

    var text: String ?= null,

    var id: String ?= null,
)
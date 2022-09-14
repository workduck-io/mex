package com.workduck.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class NamespaceMetadata(
    var icon: Icon? = null
)

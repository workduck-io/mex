package com.serverless.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PathParameter(
        val id: String?,
        val workspaceID: String?,
        val namespaceID: String?
)
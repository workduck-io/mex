package com.serverless.models.requests

data class CloneSnippetRequest(
    val snippetID: String,

    val version: Long,

    val workspaceID: String

) : WDRequest

package com.serverless.models.requests

data class CloneSnippetRequest(
    val snippetID: String,

    val workspaceID: String

) : WDRequest

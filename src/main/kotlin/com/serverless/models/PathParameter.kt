package com.serverless.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PathParameter(
    val id: String?,
    val ids: String?,
    val userID: String?,
    val nodeID: String?,
    val blockID: String?,
    val workspaceID: String?,
    val namespaceID: String?,
    val preferenceType: String?,
    val version: String?,
    val tagName: String?,
    val path: String?
)

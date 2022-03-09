package com.serverless.models.responses

data class WorkspaceResponse(
    val id: String,

    val name: String,

    val createdAt: Long?,

    val updatedAt: Long,

    val nodeHierarchy: List<String>?,

    val itemType: String = "Workspace",
) : Response

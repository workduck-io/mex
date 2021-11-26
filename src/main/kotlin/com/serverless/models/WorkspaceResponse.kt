package com.serverless.models

data class WorkspaceResponse(
    val id: String,

    val name: String,

    val createdAt: Long?,

    val updateAt: Long,

    val itemType: String = "Workspace",
) : Response

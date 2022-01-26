package com.serverless.models.responses

data class NamespaceResponse(
    val id: String,

    val name: String,

    val createdAt: Long?,

    val updatedAt: Long,

    val workspaceID: String?,

    val itemType: String = "Namespace",

) : Response

package com.serverless.models

data class NamespaceResponse(
    val id: String,

    val name: String,

    val createdAt: Long?,

    val updateAt: Long,

    val workspaceID: String?,

    val itemType: String = "Namespace",

) : Response

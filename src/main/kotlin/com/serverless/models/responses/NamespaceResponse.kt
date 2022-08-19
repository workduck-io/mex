package com.serverless.models.responses

import com.workduck.models.ItemType

data class NamespaceResponse(
    val id: String,

    val name: String,

    val createdAt: Long?,

    val updatedAt: Long,

    val itemType: String = ItemType.Namespace.name,

    val nodeHierarchy: List<String>?,

    val publicAccess: Boolean

) : Response

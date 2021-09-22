package com.workduck.models

import com.workduck.utils.Helper
import kotlin.streams.toList


enum class NodeStatus {
    LINKED,
    UNLINKED
}

data class Node(
    val id: String = Helper.generateId("NODE"),
    val version: String,
    val namespaceIdentifier: NamespaceIdentifier,
    val nodeSchemaIdentifier: NodeSchemaIdentifier,
    val status: NodeStatus = NodeStatus.LINKED,
    val associatedProperties: Set<AssociatedProperty>,
    val data: List<Element>,
    val createdAt: Long
) : Entity {
    val updatedAt: Long = System.currentTimeMillis()

    fun content(): String = data.parallelStream().map {
            element -> element.content()
    }.toList().joinToString("")
}

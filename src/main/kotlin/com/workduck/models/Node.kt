package com.workduck.models

import com.workduck.utils.Helper


enum class NodeStatus{
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
    val createdAt: Long
): Entity{
    val updatedAt : Long = System.currentTimeMillis()
}
package com.workduck.models

import com.google.common.base.Preconditions
import com.workduck.utils.Helper


enum class RelationshipStatus{
    ACTIVE,
    INACTIVE
}

data class Relationship(
    val id: String = Helper.generateId("RLSP"),
    val startNode: NodeIdentifier,
    val endNode: NodeIdentifier,
    val status: RelationshipStatus = RelationshipStatus.ACTIVE,
    val authorizations: Set<Auth>,
    val createdAt: Long
): Entity{
    val updatedAt = System.currentTimeMillis()

    init {
        Preconditions.checkArgument(startNode != endNode)
    }
}
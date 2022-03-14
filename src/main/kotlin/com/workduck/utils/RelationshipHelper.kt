package com.workduck.utils

import com.workduck.models.Relationship

object RelationshipHelper {

    fun findStartNodeOfEndNode(relationshipList: List<Relationship>, endNodeID: String): String? {
        for (relationship in relationshipList) {
            if (relationship.endNode.id == endNodeID) {
                return relationship.startNode.id
            }
        }
        return null
    }
}
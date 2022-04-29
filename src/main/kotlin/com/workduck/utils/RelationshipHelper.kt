package com.workduck.utils

import com.serverless.utils.Constants
import com.workduck.models.Relationship
import com.workduck.models.RelationshipType

object RelationshipHelper {

    fun findStartNodeOfEndNode(relationshipList: List<Relationship>, endNodeID: String): String? {
        for (relationship in relationshipList) {
            if (relationship.endNode.id == endNodeID) {
                return relationship.startNode.id
            }
        }
        return null
    }

    fun getRelationshipSK(startNodeID: String, relationshipType: RelationshipType) : String{
        return "$startNodeID${Constants.DELIMITER}${relationshipType.name}"
    }
}
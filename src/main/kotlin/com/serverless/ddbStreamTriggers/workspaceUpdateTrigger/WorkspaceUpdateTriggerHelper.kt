package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.workduck.models.NodeIdentifier
import com.workduck.models.Relationship
import com.workduck.models.RelationshipType
import com.workduck.service.RelationshipService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

object WorkspaceUpdateTriggerHelper {

    private val LOG = LogManager.getLogger(WorkspaceUpdateTriggerHelper::class.java)


    fun getNodeHierarchyInformationFromImage(listOfAttributeValue: List<AttributeValue>): List<String> {
        val nodeHierarchyInformation = mutableListOf<String>()

        for (nodePath in listOfAttributeValue) {
            nodeHierarchyInformation.add(nodePath.s)
        }
        return nodeHierarchyInformation
    }

    fun getDifferenceOfList(firstList: List<String>, secondList: List<String>): List<String> {

        /* return items in first list but not in second */
        return firstList.filterNot { secondList.contains(it) }
    }


    fun makeNodePairsAndCreateRelationships(relationshipService: RelationshipService, nodePairListForRelationship : List<Pair<String, String>>){
        val listOfRelationships = mutableListOf<Relationship>()
        for (nodePair in nodePairListForRelationship) {
            listOfRelationships.add(
                    Relationship(
                            startNode = NodeIdentifier(nodePair.first),
                            endNode = NodeIdentifier(nodePair.second),
                            type = RelationshipType.HIERARCHY
                    )
            )
        }

        LOG.info(Helper.objectMapper.writeValueAsString(listOfRelationships))

        relationshipService.createRelationshipInBatch(listOfRelationships)
    }
}
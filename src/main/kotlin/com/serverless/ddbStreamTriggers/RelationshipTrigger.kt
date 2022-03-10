package com.serverless.ddbStreamTriggers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.workduck.models.NodeIdentifier
import com.workduck.models.Relationship
import com.workduck.models.RelationshipType
import com.workduck.service.RelationshipService
import com.workduck.utils.Helper
import com.workduck.utils.Helper.splitIgnoreEmpty
import com.workduck.utils.NodeHelper.getLongestExistingPath
import com.workduck.utils.NodeHelper.getNamePath
import org.apache.logging.log4j.LogManager

class RelationshipTrigger : RequestHandler<DynamodbEvent, Void> {
    private val relationshipService = RelationshipService()

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {

        LOG.info("DYNAMODB-EVENT : $dynamodbEvent, CONTEXT : $context")
        for (record in dynamodbEvent?.records!!) {

            val newNodeHierarchyInformation =
                getNodeHierarchyInformationFromImage(record.dynamodb.newImage["nodeHierarchyInformation"]?.l ?: listOf())

            val oldNodeHierarchyInformation =
                getNodeHierarchyInformationFromImage(record.dynamodb.oldImage["nodeHierarchyInformation"]?.l ?: listOf())

            LOG.info("NEW : $newNodeHierarchyInformation")
            LOG.info("OLD : $oldNodeHierarchyInformation")

            val addedPath = getDifferenceOfList(newNodeHierarchyInformation, oldNodeHierarchyInformation)

            val removedPath = getDifferenceOfList(oldNodeHierarchyInformation, newNodeHierarchyInformation)

            /* case when more nodes added on an existing path */
            if (addedPath.size == 1 && removedPath.size == 1) {
                val commonPrefix = removedPath[0].commonPrefixWith(addedPath[0])

                /* take last node from overlapping path since that will be starting node of the first relationship to be created */
                val relationshipList: MutableList<String> = commonPrefix.split("#").takeLast(2) as MutableList<String>

                relationshipList += addedPath[0].removePrefix(commonPrefix).splitIgnoreEmpty("#")

                /* now we have a list [ node1, node1id, node2, node2id, node3, node3id.. ], we take out ids and make pairs */
                val nodePairListForRelationship = relationshipList.filterIndexed { index, _ -> index % 2 != 0 }.toList().zipWithNext()

                makeNodePairsAndCreateRelationships(nodePairListForRelationship)

            } else if (addedPath.size == 1 && removedPath.isEmpty()) {

                val longestExistingPath = getLongestExistingPath(oldNodeHierarchyInformation, getNamePath(addedPath[0]))

                var relationshipPath = addedPath[0]

                when(longestExistingPath.isNotEmpty()){
                    true -> {
                        val lastNodePath = longestExistingPath.split("#").takeLast(2).joinToString("#")
                        relationshipPath = lastNodePath + "#" + relationshipPath.removePrefix(longestExistingPath).splitIgnoreEmpty("#").joinToString("#")
                    }
                }
                println("RelationshipPath : $relationshipPath")
                val nodePairListForRelationship = relationshipPath.split("#").filterIndexed { index, _ -> index % 2 != 0 }.toList().zipWithNext()
                makeNodePairsAndCreateRelationships(nodePairListForRelationship)

            } else {
                throw Exception("Invalid case")
            }
        }
        return null
    }

    private fun makeNodePairsAndCreateRelationships(nodePairListForRelationship : List<Pair<String, String>>){
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

    private fun getNodeHierarchyInformationFromImage(listOfAttributeValue: List<AttributeValue>): List<String> {
        val nodeHierarchyInformation = mutableListOf<String>()

        for (nodePath in listOfAttributeValue) {
            nodeHierarchyInformation.add(nodePath.s)
        }
        return nodeHierarchyInformation
    }

    private fun getDifferenceOfList(firstList: List<String>, secondList: List<String>): List<String> {

        /* return items in first list but not in second */
        return firstList.filterNot { secondList.contains(it) }
    }

    companion object {
        private val LOG = LogManager.getLogger(RelationshipTrigger::class.java)
    }
}


fun main() {
    println("A#Aid#B#Bid#C#Cid".split("#").filterIndexed { index, _ -> index % 2 != 0 }.toList().zipWithNext())
}

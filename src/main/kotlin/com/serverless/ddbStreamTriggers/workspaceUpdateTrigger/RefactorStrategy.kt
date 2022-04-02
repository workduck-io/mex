package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.serverless.ddbStreamTriggers.workspaceUpdateTrigger.WorkspaceUpdateTriggerHelper.makeNodePairsAndCreateRelationships
import com.serverless.utils.commonPrefixList
import com.serverless.utils.commonSuffixList
import com.workduck.models.RelationshipType
import com.workduck.service.RelationshipService
import com.workduck.utils.NodeHelper.getIDPath
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RefactorStrategy : OperationPerformedStrategy {

    /* refactor is used to create nodes in b/w */
    override fun createRelationships(relationshipService: RelationshipService, workspaceID: String, newNodeHierarchy: List<String>, oldNodeHierarchy: List<String>, addedPath: List<String>, removedPath: List<String>) = runBlocking{
        val oldPathNewPath = oldNodeHierarchy.zip(newNodeHierarchy)
        for(pair in oldPathNewPath){
            val oldPathIDs = getIDPath(pair.first).split("#")
            val newPathIDs = getIDPath(pair.second).split("#") as MutableList<String>
            if(oldPathIDs != newPathIDs) {
                val commonPrefixNodes: List<String> = newPathIDs.commonPrefixList(oldPathIDs)
                val commonSuffixNodes: List<String> = newPathIDs.commonSuffixList(oldPathIDs) /* since the last node is at max renamed, at least one element should be there */

                newPathIDs.removeAll(commonPrefixNodes)
                newPathIDs.removeAll(commonSuffixNodes)

                /* now we just have the new nodes' ids which were created */
                println("New IDS : $newPathIDs")

                /* since we want to create relationships, to this list we add last node from prefixNodes and first node from suffix nodes */
                newPathIDs.add(0, commonPrefixNodes.last())
                newPathIDs.add(commonSuffixNodes.first())

                val nodePairListForRelationship = newPathIDs.zipWithNext()
                println(nodePairListForRelationship)

                val jobToGetRelationshipToDelete = async {  relationshipService.getRelationship(commonPrefixNodes.last(), commonSuffixNodes.first(), workspaceID, RelationshipType.HIERARCHY) }


                launch { makeNodePairsAndCreateRelationships(relationshipService, workspaceID, nodePairListForRelationship) }

                launch { relationshipService.deleteRelationship(jobToGetRelationshipToDelete.await()) }

            }
        }
    }
}

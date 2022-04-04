package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.serverless.ddbStreamTriggers.workspaceUpdateTrigger.WorkspaceUpdateTriggerHelper.makeNodePairsAndCreateRelationships
import com.serverless.utils.Constants
import com.serverless.utils.commonPrefixList
import com.serverless.utils.commonSuffixList
import com.workduck.models.RelationshipType
import com.workduck.service.RelationshipService
import com.workduck.utils.NodeHelper.getIDPath
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class RefactorStrategy : OperationPerformedStrategy {

    /* refactor is used to create nodes in b/w */
    override fun createRelationships(relationshipService: RelationshipService, workspaceID: String, newNodeHierarchy: List<String>, oldNodeHierarchy: List<String>, addedPath: List<String>, removedPath: List<String>) = runBlocking{
        val oldPathNewPath = oldNodeHierarchy.zip(newNodeHierarchy)
        for(pair in oldPathNewPath){
            val oldPathIDs = getIDPath(pair.first).split(Constants.DELIMITER)
            val newPathIDs = getIDPath(pair.second).split(Constants.DELIMITER) as MutableList<String>
            if(oldPathIDs != newPathIDs) {
                val commonPrefixNodes: List<String> = newPathIDs.commonPrefixList(oldPathIDs)
                val commonSuffixNodes: List<String> = newPathIDs.commonSuffixList(oldPathIDs) /* since the last node is at max renamed, at least one element should be there */

                newPathIDs.removeAll(commonPrefixNodes)
                newPathIDs.removeAll(commonSuffixNodes)

                /* now we just have the new nodes' ids which were created */
                LOG.debug("New IDS : $newPathIDs")

                /* since we want to create relationships, to this list we add last node from prefixNodes and first node from suffix nodes */
                newPathIDs.add(0, commonPrefixNodes.last())
                newPathIDs.add(commonSuffixNodes.first())
                LOG.debug("New IDS : $newPathIDs")

                val nodePairListForRelationship = newPathIDs.zipWithNext()
                LOG.debug(nodePairListForRelationship)

                val jobToGetRelationshipToDelete = async {  relationshipService.getRelationship(getNodeBeforeLastNodeOfPassedPath(oldPathIDs, commonSuffixNodes), commonSuffixNodes.first(), workspaceID, RelationshipType.HIERARCHY) }


                launch { makeNodePairsAndCreateRelationships(relationshipService, workspaceID, nodePairListForRelationship) }

                launch { relationshipService.deleteRelationship(jobToGetRelationshipToDelete.await()) }

            }
        }
    }

    private fun getNodeBeforeLastNodeOfPassedPath(oldPathIDs: List<String>, commonSuffixNodes: List<String>): String{
        val indexOfLastNodeIDOfPassedPath = oldPathIDs.indexOf(commonSuffixNodes.first())
        return oldPathIDs[indexOfLastNodeIDOfPassedPath-1]
    }

    companion object {
        private val LOG = LogManager.getLogger(RefactorStrategy::class.java)
    }
}

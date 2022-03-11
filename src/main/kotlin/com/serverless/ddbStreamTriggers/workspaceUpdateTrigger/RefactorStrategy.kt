package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.workduck.service.RelationshipService
import com.workduck.utils.Helper.commonPrefixList
import com.workduck.utils.Helper.commonSuffixList
import com.workduck.utils.NodeHelper.getIDPath

class RefactorStrategy : OperationPerformedStrategy {

    /* refactor is used to create nodes in b/w */
    override fun createRelationships(relationshipService: RelationshipService, newNodeHierarchy: List<String>, oldNodeHierarchy: List<String>, addedPath: List<String>, removedPath: List<String>) {
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
                WorkspaceUpdateTriggerHelper.makeNodePairsAndCreateRelationships(relationshipService, nodePairListForRelationship)

            }
        }
    }
}

fun main(){
    val new = listOf("A#NODE1#B#NODE2#C#NODE3", "E#NODE4", "A#NODE1#B#NODE2#F#NODE_7YDAdiEy6TjrEbniEALDx#X#NODE_YKLY3zQQp4nNzrPqR9mVt#E#NODE5")
    val old = listOf("A#NODE1#B#NODE2#C#NODE3", "E#NODE4", "A#NODE1#B#NODE2#D#NODE_YKLY3zQQp4nNzrPqR9mVt#E#NODE5")
    RefactorStrategy().createRelationships(RelationshipService(), new, old, listOf(""), listOf("") )
}

package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.serverless.ddbStreamTriggers.workspaceUpdateTrigger.WorkspaceUpdateTriggerHelper.makeNodePairsAndCreateRelationships
import com.workduck.service.RelationshipService
import com.workduck.utils.Helper.splitIgnoreEmpty
import com.workduck.utils.NodeHelper.getCommonPrefixNodePath

class AddedToLeafNodeStrategy : OperationPerformedStrategy {
    override fun createRelationships(relationshipService: RelationshipService, workspaceID: String, newNodeHierarchy: List<String>, oldNodeHierarchy: List<String>, addedPath: List<String>, removedPath: List<String>) {
        val commonPrefix = getCommonPrefixNodePath(removedPath[0], addedPath[0])

        /* take last node from overlapping path since that will be starting node of the first relationship to be created */
        val relationshipList: MutableList<String> = commonPrefix.split("#").takeLast(2) as MutableList<String>

        relationshipList += addedPath[0].removePrefix(commonPrefix).splitIgnoreEmpty("#")

        /* now we have a list [ node1, node1id, node2, node2id, node3, node3id.. ], we take out ids and make pairs */
        val nodePairListForRelationship = relationshipList.filterIndexed { index, _ -> index % 2 != 0 }.toList().zipWithNext()

        makeNodePairsAndCreateRelationships(relationshipService, workspaceID, nodePairListForRelationship)

    }
}
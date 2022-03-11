package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.serverless.ddbStreamTriggers.workspaceUpdateTrigger.WorkspaceUpdateTriggerHelper.makeNodePairsAndCreateRelationships
import com.workduck.service.RelationshipService
import com.workduck.utils.Helper.splitIgnoreEmpty
import com.workduck.utils.NodeHelper

class AddedNewPathStrategy: OperationPerformedStrategy {
    override fun createRelationships(relationshipService: RelationshipService, workspaceID: String, newNodeHierarchy: List<String>, oldNodeHierarchy: List<String>, addedPath: List<String>, removedPath: List<String>) {
        val longestExistingPath = NodeHelper.getLongestExistingPath(oldNodeHierarchy, NodeHelper.getNamePath(addedPath[0]))

        var relationshipPath = addedPath[0]

        when(longestExistingPath.isNotEmpty()){
            true -> {
                val lastNodePath = longestExistingPath.split("#").takeLast(2).joinToString("#")
                relationshipPath = lastNodePath + "#" + relationshipPath.removePrefix(longestExistingPath).splitIgnoreEmpty("#").joinToString("#")
            }
        }
        println("RelationshipPath : $relationshipPath")
        val nodePairListForRelationship = relationshipPath.split("#").filterIndexed { index, _ -> index % 2 != 0 }.toList().zipWithNext()
        makeNodePairsAndCreateRelationships(relationshipService, workspaceID, nodePairListForRelationship)
    }
}
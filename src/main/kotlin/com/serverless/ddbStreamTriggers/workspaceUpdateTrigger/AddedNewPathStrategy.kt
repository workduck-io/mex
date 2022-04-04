package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.serverless.ddbStreamTriggers.workspaceUpdateTrigger.WorkspaceUpdateTriggerHelper.makeNodePairsAndCreateRelationships
import com.serverless.utils.Constants
import com.serverless.utils.splitIgnoreEmpty
import com.workduck.service.RelationshipService
import com.workduck.utils.NodeHelper.getLongestExistingPath
import com.workduck.utils.NodeHelper.getNamePath

class AddedNewPathStrategy: OperationPerformedStrategy {
    override fun createRelationships(relationshipService: RelationshipService, workspaceID: String, newNodeHierarchy: List<String>, oldNodeHierarchy: List<String>, addedPath: List<String>, removedPath: List<String>) {
        val addedPathString = addedPath.firstOrNull() ?: throw Exception("Added path is null")
        val longestExistingPath = getLongestExistingPath(oldNodeHierarchy, getNamePath(addedPathString))
        var relationshipPath = addedPathString

        when(longestExistingPath.isNotEmpty()){
            true -> {
                val lastNodePath = longestExistingPath.split(Constants.DELIMITER).takeLast(2).joinToString(Constants.DELIMITER)
                relationshipPath = lastNodePath + Constants.DELIMITER + relationshipPath.removePrefix(longestExistingPath).splitIgnoreEmpty(Constants.DELIMITER).joinToString(Constants.DELIMITER)
            }
        }

        val nodePairListForRelationship = relationshipPath.split(Constants.DELIMITER).filterIndexed { index, _ -> index % 2 != 0 }.toList().zipWithNext()
        makeNodePairsAndCreateRelationships(relationshipService, workspaceID, nodePairListForRelationship)
    }
}
package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.serverless.utils.isLastNodeSame
import com.serverless.utils.isNewPathCreated
import com.serverless.utils.isRefactorWithNodeAddition
import com.serverless.utils.isRefactorWithPathDivision

class RelationshipCreationStrategyFactory {

    companion object {
        fun getRelationshipCreationStrategy(addedPath: List<String>, removedPath: List<String>): OperationPerformedStrategy {
            return if (addedPath.size == 1 && removedPath.size == 1) {
                if (addedPath[0].isLastNodeSame(removedPath[0])) {
                    RefactorStrategy() /* nodes in middle created via `/refactor` */
                } else {
                    AddedToLeafNodeStrategy() /* leaf node(s) were added either via `/create` or `/bulkCreate` */
                }
            } else if (addedPath.isNewPathCreated(removedPath)) {
                AddedNewPathStrategy() /* a standalone node is created via `/create` or node(s) were appended to non leaf node via `/bulkCreate` or `/create` */
            } else if (addedPath.isRefactorWithNodeAddition(removedPath) || addedPath.isRefactorWithPathDivision(removedPath)) {
                RefactorStrategy() /* nodes in middle created via `/refactor` */
            } else {
                throw Exception("Invalid case")
            }
        }
    }
}

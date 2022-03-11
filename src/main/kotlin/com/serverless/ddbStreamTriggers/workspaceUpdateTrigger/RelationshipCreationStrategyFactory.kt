package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

class RelationshipCreationStrategyFactory {

    companion object {
        fun getRelationshipCreationStrategy(addedPath: List<String>, removedPath: List<String>): OperationPerformedStrategy {
            return if (addedPath.size == 1 && removedPath.size == 1) {
                if (addedPath[0].split("#").last() == removedPath[0].split("#").last()) {
                    RefactorStrategy() /* nodes in middle created via `/refactor` */
                } else {
                    AddedToLeafNodeStrategy() /* leaf node(s) were added either via `/create` or `/bulkCreate` */
                }
            } else if (addedPath.size == 1 && removedPath.isEmpty()) {
                AddedNewPathStrategy() /* a standalone node is created via `/create` or nodes were appended to non leaf node via `/bulkCreate` */
            } else if (addedPath.size > 1 && removedPath.size > 1 && addedPath.size == removedPath.size) {
                RefactorStrategy() /* nodes in middle created via `/refactor` */
            } else {
                throw Exception("Invalid case")
            }
        }
    }
}

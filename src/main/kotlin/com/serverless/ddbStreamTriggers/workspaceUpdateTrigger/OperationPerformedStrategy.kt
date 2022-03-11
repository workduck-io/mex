package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.workduck.service.RelationshipService

interface OperationPerformedStrategy {
    fun createRelationships(relationshipService: RelationshipService, newNodeHierarchy: List<String>, oldNodeHierarchy: List<String>, addedPath: List<String>, removedPath: List<String>)
}

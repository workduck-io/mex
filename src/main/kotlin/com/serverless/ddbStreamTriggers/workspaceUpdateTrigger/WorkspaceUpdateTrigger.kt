package com.serverless.ddbStreamTriggers.workspaceUpdateTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.serverless.ddbStreamTriggers.workspaceUpdateTrigger.WorkspaceUpdateTriggerHelper.getDifferenceOfList
import com.serverless.ddbStreamTriggers.workspaceUpdateTrigger.WorkspaceUpdateTriggerHelper.getNodeHierarchyInformationFromImage
import com.workduck.service.RelationshipService
import org.apache.logging.log4j.LogManager

class WorkspaceUpdateTrigger : RequestHandler<DynamodbEvent, Void> {
    private val relationshipService = RelationshipService()

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        LOG.info("DYNAMODB-EVENT : $dynamodbEvent, CONTEXT : $context")
        if(dynamodbEvent == null || dynamodbEvent.records == null) return null /* will be the case when warmup lambda calls it */
        for (record in dynamodbEvent.records) {

            val workspaceID = record.dynamodb.newImage["PK"]?.s ?: throw Exception("Invalid Record. WorkspaceID not available")
            val newNodeHierarchyInformation =
                getNodeHierarchyInformationFromImage(record.dynamodb.newImage["nodeHierarchyInformation"]?.l ?: listOf())

            val oldNodeHierarchyInformation =
                getNodeHierarchyInformationFromImage(record.dynamodb.oldImage["nodeHierarchyInformation"]?.l ?: listOf())

            LOG.info("NEW : $newNodeHierarchyInformation")
            LOG.info("OLD : $oldNodeHierarchyInformation")

            val addedPath = getDifferenceOfList(newNodeHierarchyInformation, oldNodeHierarchyInformation)
            val removedPath = getDifferenceOfList(oldNodeHierarchyInformation, newNodeHierarchyInformation)

            val operationPerformedStrategy = RelationshipCreationStrategyFactory.getRelationshipCreationStrategy(addedPath, removedPath)
            operationPerformedStrategy.createRelationships(relationshipService, workspaceID, newNodeHierarchyInformation, oldNodeHierarchyInformation, addedPath, removedPath)

        }
        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(WorkspaceUpdateTrigger::class.java)
    }
}


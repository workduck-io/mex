package com.serverless.ddbStreamTriggers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.workduck.models.ItemStatus
import com.workduck.service.NodeService
import com.workduck.service.RelationshipService
import com.workduck.service.WorkspaceService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class NodeArchiveUnarchiveTrigger : RequestHandler<DynamodbEvent, Void> {
    private val relationshipService = RelationshipService()
    private val nodeService = NodeService()
    private val workspaceService = WorkspaceService()

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        if(dynamodbEvent == null || dynamodbEvent.records == null) return null /* will be the case when warmup lambda calls it */
        for (record in dynamodbEvent.records) {
            LOG.info(record)
            when(getOperationType(record)){

                /* Archiving the node */
                ItemStatus.ARCHIVED -> {
                    processRecord(record, ItemStatus.ACTIVE, ItemStatus.ARCHIVED)
                }

                /* Un-archiving the node */
                ItemStatus.ACTIVE -> {
                    processRecord(record, ItemStatus.ARCHIVED, ItemStatus.ACTIVE)
                }
            }
        }
        return null
    }

    private fun processRecord(record: DynamodbEvent.DynamodbStreamRecord, currentStatus: ItemStatus, expectedStatus: ItemStatus) = runBlocking {

        val nodeID = record.dynamodb.oldImage["PK"]?.s ?: throw Exception("Invalid Node Item")
        val workspaceID = record.dynamodb.oldImage["workspaceIdentifier"]?.s ?: throw Exception("Invalid Node Item")


        /* if the current node is end node in a hierarchy relationship, we need to change the relationship itemStatus */
        relationshipService.getHierarchyRelationshipsWithEndNode(workspaceID, nodeID, currentStatus).let {
            relationshipService.changeRelationshipStatus(it, expectedStatus)
        }


        if(expectedStatus == ItemStatus.ACTIVE) launch { workspaceService.refreshNodeHierarchyForWorkspace(workspaceID) }

        relationshipService.getHierarchyRelationshipsWithStartNode(workspaceID, nodeID, currentStatus)
                .map { relationship -> relationship.endNode.id }
                .let {
                    when(expectedStatus) {
                        ItemStatus.ARCHIVED -> nodeService.archiveNodes(it, workspaceID)
                        ItemStatus.ACTIVE -> nodeService.unarchiveNodes(it, workspaceID)
                    }
                }

    }


    private fun getOperationType(record: DynamodbEvent.DynamodbStreamRecord): ItemStatus {
        val oldStatus = record.dynamodb.oldImage["itemStatus"]?.s
        val newStatus = record.dynamodb.newImage["itemStatus"]?.s

        return if(oldStatus == ItemStatus.ARCHIVED.name && newStatus == ItemStatus.ACTIVE.name) ItemStatus.ACTIVE
        else if(oldStatus == ItemStatus.ACTIVE.name && newStatus == ItemStatus.ARCHIVED.name) ItemStatus.ARCHIVED
        else throw Exception("Unexpected Input : $record")
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeArchiveUnarchiveTrigger::class.java)
    }
}

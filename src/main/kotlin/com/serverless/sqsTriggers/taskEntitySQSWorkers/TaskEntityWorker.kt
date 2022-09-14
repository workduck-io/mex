package com.serverless.sqsTriggers.taskEntitySQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.serverless.models.requests.ElementRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.workduck.models.AdvancedElement
import com.workduck.service.NodeService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager
import java.time.Instant

class TaskEntityWorker : RequestHandler<DynamodbEvent, Void> {
    private var globalTaskEntityJSON: MutableMap<String?, Any?>? = null
    private val nodeService = NodeService()

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context?): Void? {
        dynamodbEvent?.records?.let {
            for (record in dynamodbEvent.records) {
                val newImage = record.dynamodb.newImage
                newImage["sk"]?.s ?: throw Exception("Invalid Record. EntityID not available")
                newImage["pk"]?.s ?: throw Exception("Invalid Record. WorkspaceID not available")
                LOG.debug("newImage $newImage")
                globalTaskEntityJSON = Helper.mapToJson(newImage).toMutableMap()
                LOG.debug("json ${globalTaskEntityJSON.toString()}")

                // 1. If the event is INSERT then append the entity to the node.
                // 2. If the event is MODIFY then update the metadata of the node and block.
                // 3. If the event is REMOVE then remove the entity ref from the node.

                when (record.eventName) {
                    Constants.DDB_STREAM_INSERT -> {
                        handleInsertEvent()
                    }
                    Constants.DDB_STREAM_MODIFY -> {
                        handleModifyEvent()
                    }
                    Constants.DDB_STREAM_REMOVE -> {
                        handleRemoveEvent()
                    }
                }
            }
        }
        return null
    }

    private fun handleInsertEvent() {
        val entityId = globalTaskEntityJSON?.get("sk").toString()
        val workspaceId = globalTaskEntityJSON?.get("pk").toString()
        val nodeId = globalTaskEntityJSON?.get("ak").toString()
        val userId = globalTaskEntityJSON?.get("userId").toString()
//        val userId = "145ce363-2d38-4c1e-8f99-608e9f6de8aa"
        val createdAtString = globalTaskEntityJSON?.get("_ct").toString()
        val createdAt = Instant.parse(createdAtString).epochSecond
        val newBlockId = Helper.generateId(Constants.BLOCK_ID_PREFIX)
        val elementList = listOf(AdvancedElement(
            id = newBlockId,
            content = entityId,
            elementType = Constants.ELEMENT_TYPE_P,
            createdBy = userId,
            createdAt = createdAt,
            lastEditedBy = userId,
            children = listOf(AdvancedElement(
                id = newBlockId,
                elementType = Constants.ELEMENT_TYPE_P,
            ))
        ))
        ElementRequest(elementList).let {
            nodeService.append(nodeId, workspaceId, userId, it as WDRequest)
        }
    }

    private fun handleModifyEvent() {
        TODO("Yet to be implemented")
    }

    private fun handleRemoveEvent() {
        val workspaceId = globalTaskEntityJSON?.get("pk").toString()
        val nodeId = globalTaskEntityJSON?.get("ak").toString()
        val userId = globalTaskEntityJSON?.get("userId").toString()
//        val userId = "145ce363-2d38-4c1e-8f99-608e9f6de8aa"
        val blockId = globalTaskEntityJSON?.get("sk").toString()
        // Fetch the node data and delete the block
        nodeService.getNode(nodeId, workspaceId, userID = userId).let {
            it?.dataOrder?.let { data -> nodeService.deleteBlockFromNode(blockId, workspaceId, nodeId, data) }
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(TaskEntityWorker::class.java)
    }
}
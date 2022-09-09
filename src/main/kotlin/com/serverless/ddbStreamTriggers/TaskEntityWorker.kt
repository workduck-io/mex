package com.serverless.ddbStreamTriggers

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
import java.util.Date

class TaskEntityWorker : RequestHandler<DynamodbEvent, Void> {
    var globalTaskEntityJSON: MutableMap<String?, Any?>? = null
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

    private fun handleInsertEvent(): Void? {
        val entityId = globalTaskEntityJSON?.get("sk").toString()
        val workspaceId = globalTaskEntityJSON?.get("pk").toString()
        val nodeId = globalTaskEntityJSON?.get("ak").toString()
//        val userId = globalTaskEntityJSON?.get("userId").toString()
        val userId = "f1c1b7f7-312b-4450-a996-48d5822d350f"
        val createdAtString = globalTaskEntityJSON?.get("_ct").toString()
        val createdAt = Instant.parse(createdAtString).epochSecond
        val nodeService = NodeService()
        val elementList = listOf(AdvancedElement(
            id = entityId,
            content = entityId,
            elementType = "p",
            createdBy = userId,
            createdAt = createdAt))
        val nodeRequest = ElementRequest(elementList)

        val map: Map<String, Any>? = nodeService.append(nodeId, workspaceId, userId, nodeRequest as WDRequest)
        LOG.debug(map.toString())
        return null
    }

    private fun handleModifyEvent(): Void? {
        TODO("Yet to be implemented")
    }

    private fun handleRemoveEvent(): Void? {
        TODO("Yet to be implemented")
    }

    companion object {
        private val LOG = LogManager.getLogger(TaskEntityWorker::class.java)
    }
}
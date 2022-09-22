package com.serverless.sqsTriggers.taskEntitySQSWorkers

import com.serverless.models.requests.ElementRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.workduck.models.AdvancedElement
import com.workduck.service.NodeService
import com.workduck.utils.Helper
import java.time.Instant

object TaskEntityHelper {
    private val nodeService = NodeService()

    fun handleInsertEvent(taskEntityJSON: MutableMap<String?, Any?>) {
        val entityId = taskEntityJSON?.get("sk").toString()
        val workspaceId = taskEntityJSON?.get("pk").toString()
        val nodeId = taskEntityJSON?.get("ak").toString()
        val userId = taskEntityJSON?.get("userId").toString()
//        val userId = "145ce363-2d38-4c1e-8f99-608e9f6de8aa"
        val createdAtString = taskEntityJSON?.get("_ct").toString()
        val createdAt = Instant.parse(createdAtString).epochSecond
        val newBlockId = Helper.generateId(Constants.BLOCK_ID_PREFIX)
        val elementList = listOf(
            AdvancedElement(
            id = newBlockId,
            content = entityId,
            elementType = Constants.ELEMENT_TYPE_P,
            createdBy = userId,
            createdAt = createdAt,
            lastEditedBy = userId,
            children = listOf(
                AdvancedElement(
                id = newBlockId,
                elementType = Constants.ELEMENT_TYPE_P,
            )
            )
        )
        )
        ElementRequest(elementList).let {
            nodeService.append(nodeId, workspaceId, userId, it as WDRequest)
        }
    }

    fun handleRemoveEvent(taskEntityJSON: MutableMap<String?, Any?>) {
        val workspaceId = taskEntityJSON?.get("pk").toString()
        val nodeId = taskEntityJSON?.get("ak").toString()
        val userId = taskEntityJSON?.get("userId").toString()
//        val userId = "145ce363-2d38-4c1e-8f99-608e9f6de8aa"
        val blockId = taskEntityJSON?.get("sk").toString()
        // Fetch the node data and delete the block
        nodeService.deleteBlockFromNode(blockId, workspaceId, nodeId, userId)
    }
}
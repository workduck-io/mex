package com.serverless.sqsNodeEventHandlers

import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Node
import com.workduck.service.NodeService
import com.workduck.utils.Helper

class Modify : Action {
    override fun apply(ddbPayload: DDBPayload, nodeService: NodeService) {
        val objectMapper = Helper.objectMapper
        val oldNode : Node = objectMapper.readValue(objectMapper.writeValueAsString(ddbPayload.OldImage))
        val newNode : Node = objectMapper.readValue(objectMapper.writeValueAsString(ddbPayload.NewImage))

        nodeService.createNodeVersion(newNode, oldNode.lastEditedBy == newNode.lastEditedBy)
    }
}
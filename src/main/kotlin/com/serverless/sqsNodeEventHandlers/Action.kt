package com.serverless.sqsNodeEventHandlers

import com.workduck.service.NodeService

interface Action {
    fun apply(ddbPayload: DDBPayload, nodeService: NodeService)
}
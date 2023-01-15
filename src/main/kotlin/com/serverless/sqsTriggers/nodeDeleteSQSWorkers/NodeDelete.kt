package com.serverless.sqsTriggers.nodeDeleteSQSWorkers

import com.workduck.models.Node
import com.workduck.service.NodeService

interface NodeDelete {

    fun deleteNode(node: Node, nodeService: NodeService)
}
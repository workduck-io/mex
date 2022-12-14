package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.workduck.models.Namespace
import com.workduck.service.NodeService

interface NamespaceDelete {

    fun deleteNamespace(namespace: Namespace, nodeService: NodeService)
}
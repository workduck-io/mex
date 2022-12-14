package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.workduck.models.Namespace
import com.workduck.service.NodeService

class DeleteNamespaceWithoutSuccessorStrategy : NamespaceDelete {
    override fun deleteNamespace(namespace: Namespace, nodeService: NodeService) {
        val nodeIDs = nodeService.getAllNodesWithNamespaceID(namespace.id, namespace.workspaceIdentifier.id)
        nodeService.softDeleteNodesInParallel(nodeIDs, namespace.workspaceIdentifier.id, namespace.createdBy!!)
    }

}

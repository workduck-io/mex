package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.workduck.models.Namespace
import com.workduck.service.NodeService

class DeleteNamespaceWithoutSuccessorStrategy : NamespaceDelete {
    override fun deleteNamespace(namespace: Namespace, nodeService: NodeService) {
        val namespaceDTO = Namespace.getNamespaceDTO(namespace)
        val nodeIDs = nodeService.getAllNodesWithNamespaceID(namespaceDTO.id, namespaceDTO.workspaceID)
        nodeService.softDeleteNodesInParallel(nodeIDs, namespaceDTO.workspaceID, namespaceDTO.createdBy)
    }

}

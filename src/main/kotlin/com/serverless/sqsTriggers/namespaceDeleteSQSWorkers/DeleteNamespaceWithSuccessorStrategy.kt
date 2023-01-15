package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.workduck.models.HierarchyUpdateAction
import com.workduck.models.Namespace
import com.workduck.service.NodeService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class DeleteNamespaceWithSuccessorStrategy : NamespaceDelete {

    companion object {
        private val LOG = LogManager.getLogger(DeleteNamespaceWithSuccessorStrategy::class.java)
    }

    override fun deleteNamespace(namespace: Namespace, nodeService: NodeService) = runBlocking {
        val successorNamespaceDTO = Namespace.getSuccessorNamespaceDTO(namespace)
        val namespaceDTO = Namespace.getNamespaceDTO(namespace)

        val nodeIDs = nodeService.getAllNodesWithNamespaceID(namespaceDTO.id, namespaceDTO.workspaceID)

        LOG.info("nodes to be moved : $nodeIDs") // TODO ( remove when the feature is stable )

        val successorNamespace = nodeService
            .namespaceService
            .getNamespaceAfterPermissionCheck(successorNamespaceDTO.id)
            ?: throw IllegalStateException("Either the successor namespace ${successorNamespaceDTO.id} is deleted or does not exist")

        /* since the owner of namespace can delete it, when we move the nodes, lastEditedBy should be set as ownerID */
        nodeService.changeNamespaceOfNodesInParallel(nodeIDs, namespaceDTO.workspaceID, namespaceDTO.id, successorNamespaceDTO.id, namespaceDTO.createdBy)

        launch { updateHierarchiesOfSuccessorNamespace(namespace, successorNamespace, nodeService) }

        updateHierarchiesOfNamespaceToBeDeleted(namespace, nodeService)

    }


    private fun updateHierarchiesOfSuccessorNamespace(
        namespace: Namespace,
        successorNamespace: Namespace,
        nodeService: NodeService
    ) {
        val newActiveHierarchy = successorNamespace.nodeHierarchyInformation + namespace.nodeHierarchyInformation
        val newArchivedHierarchy =
            successorNamespace.archivedNodeHierarchyInformation + namespace.archivedNodeHierarchyInformation

        nodeService.namespaceService.updateHierarchies(
            namespace.workspaceIdentifier.id,
            successorNamespace.id,
            HierarchyUpdateAction.APPEND,
            newActiveHierarchy,
            newArchivedHierarchy
        )
    }

    private fun updateHierarchiesOfNamespaceToBeDeleted(namespace: Namespace, nodeService: NodeService) {
        nodeService.namespaceService.updateHierarchies(
            namespace.workspaceIdentifier.id,
            namespace.id,
            HierarchyUpdateAction.REPLACE
        )
    }

}

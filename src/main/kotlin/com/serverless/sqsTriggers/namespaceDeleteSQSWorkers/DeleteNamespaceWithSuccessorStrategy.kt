package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.serverless.utils.Messages
import com.workduck.models.HierarchyUpdateAction
import com.workduck.models.Namespace
import com.workduck.service.NodeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager

class DeleteNamespaceWithSuccessorStrategy : NamespaceDelete {

    companion object {
        private val LOG = LogManager.getLogger(DeleteNamespaceWithSuccessorStrategy::class.java)
    }

    override fun deleteNamespace(namespace: Namespace, nodeService: NodeService) = runBlocking {
        require(namespace.successorNamespace != null) {
            IllegalArgumentException(Messages.INVALID_SUCCESSOR_NAMESPACE)
        }
        val nodeIDs = nodeService.getAllNodesWithNamespaceID(namespace.id, namespace.workspaceIdentifier.id)
        // TODO ( remove when the feature is stable )
        LOG.info("nodes to be moved : $nodeIDs")

        val successorNamespace = nodeService
            .namespaceService
            .getNamespaceAfterPermissionCheck(namespace.successorNamespace!!.id)
            ?: throw IllegalStateException(
                "Either the successor namespace " +
                        "${namespace.successorNamespace!!.id} is deleted or does not exist"
            )

        nodeService
            .changeNamespaceOfNodesInParallel(
                nodeIDs,
                namespace.workspaceIdentifier.id,
                namespace.id,
                successorNamespace.id,
                namespace.createdBy!!
            )

        launch { updateHierarchiesOfSuccessorNamespace(namespace, successorNamespace, nodeService) }
        withContext(Dispatchers.Default) {
            updateHierarchiesOfNamespaceToBeDeleted(
                namespace,
                nodeService
            )
        }
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
            namespace.successorNamespace!!.id,
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

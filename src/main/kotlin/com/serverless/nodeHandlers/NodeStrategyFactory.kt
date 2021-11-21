package com.serverless.nodeHandlers

import com.serverless.transformers.IdentifierTransformer
import com.serverless.transformers.NodeTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Identifier
import com.workduck.models.Node

class NodeStrategyFactory {

    companion object {

        const val getNodeObject = "GET /node/{id}"

        const val createNodeObject = "POST /node"

        /* since we're not hard deleting, just moving to archive */
        const val deleteNodeObject = "UPDATE /node/{id}"

        const val appendToNodeObject = "POST /node/{id}/append"

        const val getNodesByNamespaceObject = "GET /node/workspace/{workspaceID}/namespace/{namespaceID}"

        const val getNodesByWorkspaceObject = "GET /node/workspace/{workspaceID}"

        const val updateNodeBlock = "POST /node/{id}/blockUpdate"

        const val unarchiveNodeObject = "UPDATE /node/unarchive/{id}"

        const val deleteArchivedNodeObject = "DELETE /node/archive/{id}"

        const val getAllArchivedNodesObject = "GET /node/archive/{id}"

        const val getNodeVersionMetadata = "GET /node/{id}/version/metadata"

        private val nodeRegistry: Map<String, NodeStrategy> = mapOf(
            getNodeObject to GetNodeStrategy(),
            createNodeObject to CreateNodeStrategy(),
            deleteNodeObject to DeleteNodeStrategy(),
            appendToNodeObject to AppendToNodeStrategy(),
            getNodesByNamespaceObject to GetNodesByNamespaceStrategy(),
            getNodesByWorkspaceObject to GetNodesByWorkspaceStrategy(),
            updateNodeBlock to UpdateNodeBlockStrategy(),
            getNodeVersionMetadata to GetNodeVersionMetaDataStrategy(),
            unarchiveNodeObject to UnarchiveNodeStrategy(),
            deleteArchivedNodeObject to DeleteArchivedNodeStrategy(),
            getAllArchivedNodesObject to GetAllArchivedNodesStrategy()
        )

        fun getNodeStrategy(routeKey: String): NodeStrategy? {
            return nodeRegistry[routeKey]
        }
    }
}

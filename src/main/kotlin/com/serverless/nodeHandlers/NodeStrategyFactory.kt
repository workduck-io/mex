package com.serverless.nodeHandlers


class NodeStrategyFactory {

    companion object {

        const val getNodeObject = "GET /node/{id}"

        const val createNodeObject = "POST /node"

        /* since we're not hard deleting, just moving to archive */
        const val deleteNodeObject = "POST /node/archive"

        const val appendToNodeObject = "POST /node/{id}/append"

        const val getAllNodesObject = "GET /node/all/{id}"

        const val updateNodeBlock = "POST /node/{id}/blockUpdate"

        const val unarchiveNodeObject = "POST /node/unarchive"

        const val deleteArchivedNodeObject = "POST /node/archive/delete"

        const val getAllArchivedNodesObject = "GET /node/archive/{id}"

        const val getNodeVersionMetadata = "GET /node/{id}/version/metadata"

        const val makeNodePublicObject = "PATCH /node/makePublic/{id}"

        const val makeNodePrivateObject = "PATCH /node/makePrivate/{id}"

        const val getPublicNodeObject = "GET /node/public/{id}"

        const val copyOrMoveBlockObject = "PATCH /node/block/movement"

        const val refactorNodePathObject = "POST /node/refactor"

        const val bulkCreateNodesObject = "POST /node/bulkCreate"

        private val nodeRegistry: Map<String, NodeStrategy> = mapOf(
            getNodeObject to GetNodeStrategy(),
            createNodeObject to CreateNodeStrategy(),
            deleteNodeObject to DeleteNodeStrategy(),
            appendToNodeObject to AppendToNodeStrategy(),
            updateNodeBlock to UpdateNodeBlockStrategy(),
            getNodeVersionMetadata to GetNodeVersionMetaDataStrategy(),
            unarchiveNodeObject to UnarchiveNodeStrategy(),
            deleteArchivedNodeObject to DeleteArchivedNodeStrategy(),
            getAllArchivedNodesObject to GetAllArchivedNodesStrategy(),
            updateNodeBlock to UpdateNodeBlockStrategy(),
            makeNodePublicObject to MakeNodePublicStrategy(),
            makeNodePublicObject to MakeNodePrivateStrategy(),
            getPublicNodeObject to GetPublicNodeStrategy(),
            getAllNodesObject to GetAllNodesStrategy(),
            getPublicNodeObject to GetPublicNodeStrategy(),
            copyOrMoveBlockObject to CopyOrMoveBlockStrategy(),
            getAllNodesObject to GetAllNodesStrategy(),
            refactorNodePathObject to RefactorNodePathStrategy(),
            bulkCreateNodesObject to BulkCreateNodesStrategy()
        )

        fun getNodeStrategy(routeKey: String): NodeStrategy? {
            return nodeRegistry[routeKey]
        }
    }
}

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

        const val moveBlockObject = "PATCH /node/block/move/{blockID}/{nodeID1}/{nodeID2}"

        const val copyBlockObject = "PATCH /node/block/copy/{blockID}/{nodeID1}/{nodeID2}"

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
            moveBlockObject to MoveBlockStrategy(),
            copyBlockObject to CopyBlockStrategy()
        )

        fun getNodeStrategy(routeKey: String): NodeStrategy? {
            return nodeRegistry[routeKey]
        }
    }
}

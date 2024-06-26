package com.serverless.nodeHandlers


class NodeStrategyFactory {

    companion object {

        const val getNodeObject = "GET /node/{id}"

        const val batchGetNodeObject = "POST /node/ids"

        const val createNodeObject = "POST /node"

        const val adminCreateNodeObject = "POST /node/admin/{id}"

        const val createNodeObjectV2 = "POST /v2/node"

        /* since we're not hard deleting, just moving to archive */
        const val archiveNodeObject = "PUT /node/archive"

        const val archiveNodeMiddlewareObject = "PUT /node/archive/middleware"

        const val appendToNodeObject = "PATCH /node/{id}/append"

        const val getAllNodesObject = "GET /node/all/{id}"

        const val updateNodeBlock = "PATCH /node/{id}/block"

        const val getMetadataOfNodes = "GET /node/metadata"

        const val unarchiveNodeObject = "PUT /node/unarchive"

        const val deleteArchivedNodeObject = "POST /node/archive/delete"

        const val getAllArchivedNodesObject = "GET /node/archive"

        const val makeNodePublicObject = "PATCH /node/makePublic/{id}"

        const val makeNodePrivateObject = "PATCH /node/makePrivate/{id}"

        const val getPublicNodeObject = "GET /node/public/{id}"

        const val copyOrMoveBlockObject = "PATCH /node/block/movement"

        const val refactorNodePathObject = "POST /node/refactor"

        const val bulkCreateNodesObject = "POST /node/bulk"

        const val shareNode = "POST /shared/node"

        const val getSharedNode = "GET /shared/node/{nodeID}"

        const val revokeNodeAccess = "DELETE /shared/node"

        const val updateSharedNodeAccess = "PUT /shared/node"

        const val updateSharedNode = "POST /shared/node/update"

        const val getAllSharedUsers = "GET /shared/node/{id}/users"

        const val getAccessDataForUser = "GET /shared/node/{id}/access"

        const val getAllSharedNodes = "GET /shared/node/all"

        const val deleteBlockObject = "PATCH /node/{id}/delete/block"

        const val updateMetadata = "PATCH /node/metadata/{id}"

        private val nodeRegistry: Map<String, NodeStrategy> = mapOf(
            deleteBlockObject to DeleteBlockStrategy(),
            getNodeObject to GetNodeStrategy(),
            batchGetNodeObject to GetNodesByIDStrategy(),
            createNodeObject to CreateNodeStrategy(),
            adminCreateNodeObject to AdminCreateNodeStrategy(),
            createNodeObjectV2 to CreateNodeStrategyV2(),
            archiveNodeObject to ArchiveNodeStrategy(),
            archiveNodeMiddlewareObject to ArchiveNodeMiddlewareStrategy(),
            appendToNodeObject to AppendToNodeStrategy(),
            updateNodeBlock to UpdateNodeBlockStrategy(),
            getMetadataOfNodes to GetNodesMetadataStrategy(),
            unarchiveNodeObject to UnarchiveNodeStrategy(),
            deleteArchivedNodeObject to DeleteArchivedNodeStrategy(),
            getAllArchivedNodesObject to GetAllArchivedNodesStrategy(),
            makeNodePublicObject to MakeNodePublicStrategy(),
            makeNodePrivateObject to MakeNodePrivateStrategy(),
            getPublicNodeObject to GetPublicNodeStrategy(),
            getAllNodesObject to GetAllNodesStrategy(),
            copyOrMoveBlockObject to CopyOrMoveBlockStrategy(),
            refactorNodePathObject to RefactorNodePathStrategy(),
            bulkCreateNodesObject to BulkCreateNodesStrategy(),
            shareNode to ShareNodeStrategy(),
            getSharedNode to GetSharedNodeStrategy(),
            revokeNodeAccess to RevokeNodeAccessStrategy(),
            updateSharedNodeAccess to UpdateSharedNodeAccessStrategy(),
            updateSharedNode to UpdateSharedNodeStrategy(),
            getAllSharedUsers to GetAllSharedUsersStrategy(),
            getAccessDataForUser to GetAccessDataForUserStrategy(),
            getAllSharedNodes to GetAllSharedNodesStrategy(),
            updateMetadata to UpdateMetadataStrategy()
        )

        fun getNodeStrategy(routeKey: String): NodeStrategy? {
            return nodeRegistry[routeKey.replace("/v1", "")]
        }
    }
}

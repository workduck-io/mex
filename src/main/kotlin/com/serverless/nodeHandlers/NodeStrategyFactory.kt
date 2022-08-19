package com.serverless.nodeHandlers


class NodeStrategyFactory {

    companion object {

        const val getNodeObject = "GET /node/{id}"

        const val createNodeObject = "POST /node"

        /* since we're not hard deleting, just moving to archive */
        const val archiveNodeObject = "PUT /node/archive"

        const val archiveNodeMiddlewareObject = "PUT /node/archive/middleware"

        const val appendToNodeObject = "POST /node/{id}/append"

        const val getAllNodesObject = "GET /node/all/{id}"

        const val updateNodeBlock = "POST /node/{id}/blockUpdate"

        const val getMetadataOfNodes = "GET /node/metadata"

        const val unarchiveNodeObject = "PUT /node/unarchive"

        const val deleteArchivedNodeObject = "POST /node/archive/delete"

        const val getAllArchivedNodesObject = "GET /node/archive"

        const val getNodeVersionMetadata = "GET /node/{id}/version/metadata"

        const val makeNodePublicObject = "PATCH /node/makePublic/{id}"

        const val makeNodePrivateObject = "PATCH /node/makePrivate/{id}"

        const val getPublicNodeObject = "GET /node/public/{id}"

        const val copyOrMoveBlockObject = "PATCH /node/block/movement"

        const val refactorNodePathObject = "POST /node/refactor"

        const val bulkCreateNodesObject = "POST /node/bulkCreate"

        const val shareNode = "POST /shared/node"

        const val getSharedNode = "GET /shared/node/{nodeID}"

        const val revokeNodeAccess = "DELETE /shared/node"

        const val updateSharedNodeAccess = "PUT /shared/node"

        const val updateSharedNode = "POST /shared/node/update"

        const val getAllSharedUsers = "GET /shared/node/{id}/users"

        const val getAccessDataForUser = "GET /shared/node/{id}/access"

        const val getAllSharedNodes = "GET /shared/node/all"

        private val nodeRegistry: Map<String, NodeStrategy> = mapOf(
            getNodeObject to GetNodeStrategy(),
            createNodeObject to CreateNodeStrategy(),
            archiveNodeObject to ArchiveNodeStrategy(),
            archiveNodeMiddlewareObject to ArchiveNodeMiddlewareStrategy(),
            appendToNodeObject to AppendToNodeStrategy(),
            updateNodeBlock to UpdateNodeBlockStrategy(),
            getNodeVersionMetadata to GetNodeVersionMetaDataStrategy(),
            getMetadataOfNodes to GetNodesMetadataStrategy(),
            unarchiveNodeObject to UnarchiveNodeStrategy(),
            deleteArchivedNodeObject to DeleteArchivedNodeStrategy(),
            getAllArchivedNodesObject to GetAllArchivedNodesStrategy(),
            updateNodeBlock to UpdateNodeBlockStrategy(),
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
            getAllSharedNodes to GetAllSharedNodesStrategy()
        )

        fun getNodeStrategy(routeKey: String): NodeStrategy? {
            return nodeRegistry[routeKey]
        }
    }
}

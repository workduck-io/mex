package com.serverless.namespaceHandlers


class NamespaceStrategyFactory {

    companion object {

        const val getNamespaceObject = "GET /namespace/{id}"

        const val createNamespaceObject = "POST /namespace"

        const val updateNamespaceObject = "PATCH /namespace"

        const val deleteNamespaceObject = "DELETE /namespace/{id}"

        const val getAllNamespaceDataObject = "GET /namespace/all"

        const val getAllNamespaceDataV2Object = "GET /v2/namespace/all"

        const val makeNamespacePublicObject = "PATCH /namespace/makePublic/{id}"

        const val makeNamespacePrivateObject = "PATCH /namespace/makePrivate/{id}"

        const val getPublicNamespaceObject = "GET /namespace/public/{id}"

        const val getNodeHierarchyObject = "GET /namespace/all/hierarchy"

        const val shareNamespace = "POST /shared/namespace"

        const val revokeNamespaceAccess = "DELETE /shared/namespace"

        const val getAllSharedUsers = "GET /shared/namespace/{id}/users"

        const val getAccessDataForUser = "GET /shared/namespace/{id}/access"

        const val getNodeIDFromPath = "GET /namespace/{namespaceID}/path/{nodeID}/{path}"

        private val namespaceRegistry: Map<String, NamespaceStrategy> = mapOf(
            getNamespaceObject to GetNamespaceStrategy(),
            createNamespaceObject to CreateNamespaceStrategy(),
            updateNamespaceObject to UpdateNamespaceStrategy(),
            deleteNamespaceObject to DeleteNamespaceStrategy(),
            getAllNamespaceDataObject to GetAllNamespaceDataStrategy(),
            getAllNamespaceDataV2Object to GetAllNamespaceDataV2Strategy(),
            makeNamespacePublicObject to MakeNamespacePublicStrategy(),
            makeNamespacePrivateObject to MakeNamespacePrivateStrategy(),
            getPublicNamespaceObject to GetPublicNamespaceStrategy(),
            getNodeHierarchyObject to GetHierarchyStrategy(),
            shareNamespace to ShareNamespaceStrategy(),
            revokeNamespaceAccess to RevokeNamespaceAccessStrategy(),
            getAllSharedUsers to GetAllSharedUsersStrategy(),
            getAccessDataForUser to GetAccessDataForUserStrategy(),
            getNodeIDFromPath to GetNodeIDFromPathStrategy()
        )

        fun getNamespaceStrategy(routeKey: String): NamespaceStrategy? {
            return namespaceRegistry[routeKey.replace("/v1", "")]
        }
    }
}

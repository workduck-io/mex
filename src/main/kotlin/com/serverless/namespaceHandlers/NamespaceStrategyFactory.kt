package com.serverless.namespaceHandlers

class NamespaceStrategyFactory {

    companion object {

        const val getNamespaceObject = "GET /namespace/{id}"

        const val createNamespaceObject = "POST /namespace"

        const val updateNamespaceObject = "PATCH /namespace"

        const val deleteNamespaceObject = "DELETE /namespace/{id}"

        const val getNamespaceDataObject = "GET /namespace/all"

        const val makeNamespacePublicObject = "PATCH /namespace/makePublic/{id}"

        const val makeNamespacePrivateObject = "PATCH /namespace/makePrivate/{id}"

        const val getPublicNamespaceObject = "GET /namespace/public/{id}"

        const val getNodeHierarchyObject = "GET /namespace/all/hierarchy"

        const val shareNamespace = "POST /shared/namespace"

        const val revokeNamespaceAccess = "DELETE /shared/namespace"

        const val getAllSharedNamespaces = "GET /shared/namespace/all"

        const val getAllSharedUsers = "GET /shared/namespace/{id}/users"

        private val namespaceRegistry: Map<String, NamespaceStrategy> = mapOf(
            getNamespaceObject to GetNamespaceStrategy(),
            createNamespaceObject to CreateNamespaceStrategy(),
            updateNamespaceObject to UpdateNamespaceStrategy(),
            deleteNamespaceObject to DeleteNamespaceStrategy(),
            getNamespaceDataObject to GetAllNamespaceDataStrategy(),
            makeNamespacePublicObject to MakeNamespacePublicStrategy(),
            makeNamespacePrivateObject to MakeNamespacePrivateStrategy(),
            getPublicNamespaceObject to GetPublicNamespaceStrategy(),
            getNodeHierarchyObject to GetHierarchyStrategy(),
            shareNamespace to ShareNamespaceStrategy(),
            revokeNamespaceAccess to RevokeNamespaceAccessStrategy(),
            getAllSharedNamespaces to GetAllSharedNamespacesStrategy(),
            getAllSharedUsers to GetAllSharedUsersStrategy()
        )

        fun getNamespaceStrategy(routeKey: String): NamespaceStrategy? {
            return namespaceRegistry[routeKey.replace("/v1", "")]
        }
    }
}

package com.serverless.namespaceHandlers

import com.serverless.snippetHandlers.GetAllSnippetsOfWorkspaceStrategy


class NamespaceStrategyFactory {

    companion object {

        const val getNamespaceObject = "GET /namespace/{id}"

        const val createNamespaceObject = "POST /namespace"

        const val renameNamespaceObject = "PATCH /namespace/{id}"

        const val deleteNamespaceObject = "DELETE /namespace/{id}"

        const val getNamespaceDataObject = "GET /namespace/all"

        const val makeNamespacePublicObject = "PATCH /namespace/makePublic/{id}"

        const val makeNamespacePrivateObject = "PATCH /namespace/makePrivate/{id}"

        const val getPublicNamespaceObject = "GET /namespace/public/{id}"

        const val getNodeHierarchyObject = "GET /namespace/all/hierarchy"

        private val namespaceRegistry: Map<String, NamespaceStrategy> = mapOf(
            getNamespaceObject to GetNamespaceStrategy(),
            createNamespaceObject to CreateNamespaceStrategy(),
            renameNamespaceObject to RenameNamespaceStrategy(),
            deleteNamespaceObject to DeleteNamespaceStrategy(),
            getNamespaceDataObject to GetAllNamespaceDataStrategy(),
            makeNamespacePublicObject to MakeNamespacePublicStrategy(),
            makeNamespacePrivateObject to MakeNamespacePrivateStrategy(),
            getPublicNamespaceObject to GetPublicNamespaceStrategy(),
            getNodeHierarchyObject to GetHierarchyStrategy()
        )

        fun getNamespaceStrategy(routeKey: String): NamespaceStrategy? {
            return namespaceRegistry[routeKey]
        }
    }
}

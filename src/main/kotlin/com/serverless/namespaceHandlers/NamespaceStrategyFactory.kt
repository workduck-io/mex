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

        private val namespaceRegistry: Map<String, NamespaceStrategy> = mapOf(
            getNamespaceObject to GetNamespaceStrategy(),
            createNamespaceObject to CreateNamespaceStrategy(),
            updateNamespaceObject to UpdateNamespaceStrategy(),
            deleteNamespaceObject to DeleteNamespaceStrategy(),
            getNamespaceDataObject to GetAllNamespaceDataStrategy(),
            makeNamespacePublicObject to MakeNamespacePublicStrategy(),
            makeNamespacePrivateObject to MakeNamespacePrivateStrategy(),
            getPublicNamespaceObject to GetPublicNamespaceStrategy(),
            getNodeHierarchyObject to GetPublicNamespaceStrategy()
        )

        fun getNamespaceStrategy(routeKey: String): NamespaceStrategy? {
            return namespaceRegistry[routeKey]
        }
    }
}

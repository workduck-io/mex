package com.serverless.namespaceHandlers


class NamespaceStrategyFactory {

    companion object {

        const val getNamespaceObject = "GET /namespace/{id}"

        const val createNamespaceObject = "POST /namespace"

        const val updateNamespaceObject = "POST /namespace/update"

        const val deleteNamespaceObject = "DELETE /namespace/{id}"

        const val getNamespaceDataObject = "GET /namespace/all"

        const val makeNamespacePublicObject = "PATCH /namespace/makePublic/{id}"

        const val makeNamespacePrivateObject = "PATCH /namespace/makePrivate/{id}"

        const val getPublicNamespaceObject = "GET /namespace/public/{id}"

        private val namespaceRegistry: Map<String, NamespaceStrategy> = mapOf(
            getNamespaceObject to GetNamespaceStrategy(),
            createNamespaceObject to CreateNamespaceStrategy(),
            updateNamespaceObject to UpdateNamespaceStrategy(),
            deleteNamespaceObject to DeleteNamespaceStrategy(),
            getNamespaceDataObject to GetAllNamespaceDataStrategy(),
            makeNamespacePublicObject to MakeNamespacePublicStrategy(),
            makeNamespacePrivateObject to MakeNamespacePrivateStrategy()
        )

        fun getNamespaceStrategy(routeKey: String): NamespaceStrategy? {
            return namespaceRegistry[routeKey]
        }
    }
}

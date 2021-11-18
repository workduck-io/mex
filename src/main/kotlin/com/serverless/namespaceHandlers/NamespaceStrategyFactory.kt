package com.serverless.namespaceHandlers

import com.serverless.transformers.IdentifierTransformer
import com.serverless.transformers.NamespaceTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Identifier
import com.workduck.models.Namespace

class NamespaceStrategyFactory {

    companion object {

        val namespaceTransformer : Transformer<Namespace> = NamespaceTransformer()

        val identifierTransformer : Transformer<Identifier> = IdentifierTransformer()

        const val getNamespaceObject = "GET /namespace/{id}"

        const val createNamespaceObject = "POST /namespace"

        const val updateNamespaceObject = "POST /namespace/update"

        const val deleteNamespaceObject = "DELETE /namespace/{id}"

        const val getNamespaceDataObject = "GET /namespace/data/{ids}"

        private val namespaceRegistry: Map<String, NamespaceStrategy> = mapOf(
            getNamespaceObject to GetNamespaceStrategy(namespaceTransformer),
            createNamespaceObject to CreateNamespaceStrategy(namespaceTransformer),
            updateNamespaceObject to UpdateNamespaceStrategy(namespaceTransformer),
            deleteNamespaceObject to DeleteNamespaceStrategy(identifierTransformer),
            getNamespaceDataObject to GetNamespaceDataStrategy(namespaceTransformer)
        )

        fun getNamespaceStrategy(routeKey: String): NamespaceStrategy? {
            return namespaceRegistry[routeKey]
        }
    }
}

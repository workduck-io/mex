package com.serverless.transformers

import com.serverless.models.responses.NamespaceResponse
import com.serverless.models.responses.Response
import com.workduck.models.Namespace

class NamespaceTransformer : Transformer<Namespace> {

    override fun transform(t: Namespace?): Response? = t?.let {
       NamespaceResponse(
            id = t.id,
            name = t.name,
            nodeHierarchy = t.nodeHierarchyInformation,
            archiveNodeHierarchy = t.archivedNodeHierarchyInformation,
            metadata = t.namespaceMetadata,
            createdAt = t.createdAt,
            updatedAt = t.updatedAt,
            publicAccess= t.publicAccess
       )
    }
}

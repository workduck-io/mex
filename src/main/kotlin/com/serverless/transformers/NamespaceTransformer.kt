package com.serverless.transformers

import com.serverless.models.NamespaceResponse
import com.serverless.models.Response
import com.workduck.models.Namespace


class NamespaceTransformer : Transformer<Namespace> {

    override fun transform(t: Namespace?): Response? {
        if(t == null) return null
        return NamespaceResponse(id = t.id,
                name = t.name,
                workspaceID = t.workspaceIdentifier?.id,
                createdAt = t.createdAt,
                updateAt = t.updatedAt)
    }
}
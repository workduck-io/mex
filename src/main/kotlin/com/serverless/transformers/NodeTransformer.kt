package com.serverless.transformers

import com.serverless.models.NodeResponse
import com.serverless.models.Response
import com.workduck.models.Node

class NodeTransformer : Transformer<Node> {

    override fun transform(t: Node?): Response? {
        if (t == null) return null
        return NodeResponse(id = t.id,
                data = t.data,
                lastEditedBy = t.lastEditedBy ,
                createdBy = t.createBy,
                createdAt = t.createdAt ,
                updateAt = t.updatedAt,
                version = t.version,
                namespaceID = t.namespaceIdentifier?.id,
                workspaceID = t.workspaceIdentifier?.id)
    }
}
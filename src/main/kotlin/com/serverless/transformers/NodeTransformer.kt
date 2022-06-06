package com.serverless.transformers

import com.serverless.models.responses.NodeResponse
import com.serverless.models.responses.Response
import com.workduck.models.Node

class NodeTransformer : Transformer<Node> {

    override fun transform(t: Node?): Response? = t?.let {
       NodeResponse(
            id = t.id,
            data = t.data,
            lastEditedBy = t.lastEditedBy,
            createdBy = t.createdBy,
            createdAt = t.createdAt,
            updatedAt = t.updatedAt,
            version = t.version,
            tags = t.tags,
            namespaceID = t.namespaceIdentifier?.id,
            workspaceID = t.workspaceIdentifier?.id,
            isBookmarked = t.isBookmarked,
            publicAccess = t.publicAccess,
//            saveableRange = t.saveableRange,
//            sourceUrl = t.sourceUrl
       )
    }
}

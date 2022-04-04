package com.serverless.transformers

import com.serverless.models.responses.Response
import com.serverless.models.responses.WorkspaceResponse
import com.workduck.models.Workspace

class WorkspaceTransformer : Transformer<Workspace> {

    override fun transform(t: Workspace?): Response?= t?.let {
        WorkspaceResponse(
            id = t.id,
            name = t.name,
            createdAt = t.createdAt,
            updatedAt = t.updatedAt,
            nodeHierarchy = t.nodeHierarchyInformation ?: listOf()
        )
    }
}

package com.serverless.transformers

import com.serverless.models.Response
import com.serverless.models.WorkspaceResponse
import com.workduck.models.Workspace

class WorkspaceTransformer : Transformer<Workspace> {

    override fun transform(t: Workspace?): Response?= t?.let {
        WorkspaceResponse(
            id = t.id,
            name = t.name,
            createdAt = t.createdAt,
            updateAt = t.updatedAt
        )
    }
}

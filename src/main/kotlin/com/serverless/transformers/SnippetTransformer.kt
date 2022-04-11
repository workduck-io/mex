package com.serverless.transformers

import com.serverless.models.responses.Response
import com.serverless.models.responses.SnippetResponse
import com.workduck.models.Snippet

class SnippetTransformer : Transformer<Snippet> {
    override fun transform(t: Snippet?): Response? = t?.let {
        SnippetResponse(
                id = t.id,
                data = t.data,
                lastEditedBy = t.lastEditedBy,
                createdBy = t.createdBy,
                createdAt = t.createdAt,
                updatedAt = t.updatedAt,
                version = t.version,
        )
    }


}
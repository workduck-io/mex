package com.serverless.transformers

import com.serverless.models.IdentifierResponse
import com.serverless.models.Response
import com.workduck.models.Identifier

class IdentifierTransformer : Transformer<Identifier> {

    override fun transform(t: Identifier?): Response? {
        if (t == null) return null
        return IdentifierResponse(
            id = t.id,
            type = t.type.name
        )
    }
}
package com.serverless.transformers

import com.serverless.models.responses.IdentifierResponse
import com.serverless.models.responses.Response
import com.workduck.models.Identifier

class IdentifierTransformer : Transformer<Identifier> {

    override fun transform(t: Identifier?): Response? = t?.let{
        IdentifierResponse(
                id = t.id,
                type = t.type.name
        )
    }
}

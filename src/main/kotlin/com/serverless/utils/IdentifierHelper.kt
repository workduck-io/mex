package com.serverless.utils

import com.serverless.models.responses.Response
import com.serverless.transformers.IdentifierTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Identifier

object IdentifierHelper {

    val identifierTransformer : Transformer<Identifier> = IdentifierTransformer()

    fun convertIdentifierToIdentifierResponse(identifier: Identifier?) : Response? {
        return identifierTransformer.transform(identifier)
    }
}
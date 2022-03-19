package com.serverless.utils

import com.serverless.models.responses.Response
import com.serverless.transformers.SnippetTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Snippet

object SnippetHelper {

    val snippetTransformer : Transformer<Snippet> = SnippetTransformer()

    fun convertSnippetToSnippetResponse(snippet : Entity?) : Response? {
        return snippetTransformer.transform(snippet as Snippet?)
    }
}
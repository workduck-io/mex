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

    fun getSnippetVersion(version : String) : Long{
        return if(version.lowercase() == "latest") -1
        else if (version.toDoubleOrNull() != null){
            when(version.matches("\\d+".toRegex())){
                false -> throw IllegalArgumentException("Enter a whole number")
                true -> {
                    when(version.toLong() > 0 ){
                        true -> version.toLong()
                        false -> throw IllegalArgumentException("Enter a positive number")

                    }
                }
            }
        }
        else throw IllegalArgumentException("Enter a valid version")

    }
}

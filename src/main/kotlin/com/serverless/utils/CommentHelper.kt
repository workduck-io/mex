package com.serverless.utils

import com.serverless.models.Response
import com.serverless.transformers.CommentTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Comment

object CommentHelper {

    val commentTransformer : Transformer<Comment> = CommentTransformer()

    fun convertCommentToCommentResponse(comment: Comment?) : Response?{
        return commentTransformer.transform(comment)
    }


    fun getEntityIDFromPk(pk : String) : String = pk.split("#")[0]


}
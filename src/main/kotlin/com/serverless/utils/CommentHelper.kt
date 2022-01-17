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

    fun getNodeIDFromPk(pk : String) : String{
        return pk.split("#")[0]
    }

    fun getBlockIDFromSk(sk : String) : String{
        return sk.split("#")[0]
    }

    fun getCommentIDFromSk(sk : String) : String{
        return sk.split("#")[1]
    }
}
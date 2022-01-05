package com.serverless.utils

import com.serverless.models.CommentResponse
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
        val list = pk.split("#")
        return list[0]
    }

    fun getBlockIDFromSk(sk : String) : String{
        val list = sk.split("#")
        return list[0]
    }

    fun getCommentIDFromSk(sk : String) : String{
        val list = sk.split("#")
        return list[1]
    }
}
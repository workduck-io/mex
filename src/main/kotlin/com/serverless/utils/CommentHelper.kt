package com.serverless.utils

import com.serverless.models.responses.Response
import com.serverless.transformers.CommentTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Comment

object CommentHelper {

    val commentTransformer : Transformer<Comment> = CommentTransformer()

    fun convertCommentToCommentResponse(comment: Comment?) : Response?{
        return commentTransformer.transform(comment)
    }


    fun getEntityIDFromPk(pk : String) : String = pk.split(Constants.DELIMITER)[0]

    fun isBlockOrNodeID(entityID : String) : Boolean {
        return entityID.startsWith("NODE") || entityID.startsWith("TEMP") ||  entityID.startsWith("SYNC")
    }

    fun isValidEntity(entityID: String) : Boolean {
        return isBlockOrNodeID(entityID) || entityID.startsWith("USER")
    }
}
package com.serverless.transformers

import com.serverless.models.CommentResponse
import com.serverless.models.Response
import com.workduck.models.Comment

class CommentTransformer : Transformer<Comment> {
    override fun transform(t: Comment?): Response? {
        if(t == null) return null
        return CommentResponse(
            nodeID = getNodeIDFromPk(t.pk),
            blockID = getBlockIDFromSk(t.sk),
            commentID = getCommentIDFromSk(t.sk),
            commentedBy = t.commentedBy,
            commentBody = t.commentBody,
            createdAt = t.createdAt,
            updatedAt = t.updatedAt
        )
    }



    private fun getNodeIDFromPk(pk : String) : String{
        val list = pk.split("#")
        return list[0]
    }

    private fun getBlockIDFromSk(sk : String) : String{
        val list = sk.split("#")
        return list[0]
    }

    private fun getCommentIDFromSk(sk : String) : String{
        val list = sk.split("#")
        return list[1]
    }
}
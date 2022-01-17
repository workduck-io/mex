package com.serverless.transformers

import com.serverless.models.CommentResponse
import com.serverless.models.Response
import com.serverless.utils.CommentHelper
import com.workduck.models.Comment

class CommentTransformer : Transformer<Comment> {
    override fun transform(t: Comment?): Response? = t?.let{
        CommentResponse(
                nodeID = CommentHelper.getNodeIDFromPk(t.pk),
                blockID = CommentHelper.getBlockIDFromSk(t.sk),
                commentID = CommentHelper.getCommentIDFromSk(t.sk),
                commentedBy = t.commentedBy?.id,
                commentBody = t.commentBody,
                createdAt = t.createdAt,
                updatedAt = t.updatedAt
        )
    }

}

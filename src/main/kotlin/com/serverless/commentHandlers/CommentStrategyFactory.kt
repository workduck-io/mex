package com.serverless.commentHandlers

class CommentStrategyFactory {

    companion object{
        const val createComment = "POST /comment"

        const val getComment = "GET /comment/{id}"

        const val editComment = "PATCH /comment"

        const val deleteComment = "DELETE /comment/{id}"

        const val getAllComments = "GET /comment/all/{id}"


        private val commentRegistry: Map<String, CommentStrategy> = mapOf(
                createComment to CreateCommentStrategy(),
                getComment to GetCommentStrategy(),
                editComment to UpdateCommentStrategy(),
                deleteComment to DeleteCommentStrategy(),
                getAllComments to GetAllCommentsStrategy(),
        )


        fun getCommentStrategy(routeKey: String): CommentStrategy? {
            return commentRegistry[routeKey.replace("/v1", "")]
        }


    }
}
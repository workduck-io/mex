package com.serverless.commentHandlers

class CommentStrategyFactory {

    companion object{
        const val createComment = "POST /comment"

        const val getComment = "GET /comment/{id}"

        const val editComment = "UPDATE /comment/{id}"

        const val deleteComment = "DELETE /comment/{id}"

        const val getAllCommentsOfNode = "GET /comment/node/{id}"


        private val commentRegistry: Map<String, CommentStrategy> = mapOf(
                createComment to CreateCommentStrategy(),
                getComment to GetCommentStrategy(),
                editComment to UpdateCommentStrategy(),
                deleteComment to DeleteCommentStrategy(),
                getAllCommentsOfNode to GetAllCommentsOfNodeStrategy()
        )


        fun getCommentStrategy(routeKey: String): CommentStrategy? {
            return CommentStrategyFactory.commentRegistry[routeKey]
        }


    }
}
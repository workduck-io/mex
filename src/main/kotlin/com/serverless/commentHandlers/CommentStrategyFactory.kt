package com.serverless.commentHandlers

class CommentStrategyFactory {

    companion object{
        const val createComment = "POST /comment"

        const val getComment = "GET /comment"

        const val editComment = "PATCH /comment"

        const val deleteComment = "DELETE /comment"

        const val getAllCommentsOfNode = "GET /comment/node/{id}"

        const val getAllCommentsOfBlock = "GET /comment/node/{nodeID}/block/{blockID}"


        private val commentRegistry: Map<String, CommentStrategy> = mapOf(
                createComment to CreateCommentStrategy(),
                getComment to GetCommentStrategy(),
                editComment to UpdateCommentStrategy(),
                deleteComment to DeleteCommentStrategy(),
                getAllCommentsOfNode to GetAllCommentsOfNodeStrategy(),
                getAllCommentsOfBlock to GetAllCommentsOfBlockStrategy()
        )


        fun getCommentStrategy(routeKey: String): CommentStrategy? {
            return commentRegistry[routeKey]
        }


    }
}
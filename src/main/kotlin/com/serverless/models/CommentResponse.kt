package com.serverless.models

import com.workduck.models.AdvancedElement

data class CommentResponse(
        val nodeID : String,

        val commentID : String,

        val blockID : String,

        val commentedBy : String,

        val commentBody : AdvancedElement?,

        val updatedAt : Long,

        val createdAt : Long?
) : Response

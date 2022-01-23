package com.serverless.models

import com.workduck.models.AdvancedElement

data class CommentResponse(
        //val nodeID : String,

        val entityID : String,

        val commentID : String,

        val commentedBy : String?,

        val commentBody : AdvancedElement?,

        val updatedAt : Long,

        val createdAt : Long?
) : Response

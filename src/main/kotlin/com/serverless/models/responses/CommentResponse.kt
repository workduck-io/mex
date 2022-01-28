package com.serverless.models.responses

import com.workduck.models.AdvancedElement

data class CommentResponse(

        val entityID : String,

        val commentID : String,

        val commentedBy : String?,

        val commentBody : AdvancedElement?,

        override val updatedAt : Long,

        override val createdAt : Long
) : Response , TimestampAdhereResponse

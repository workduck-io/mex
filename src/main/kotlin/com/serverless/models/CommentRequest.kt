package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.models.AdvancedElement
import com.workduck.models.IdentifierType
import com.workduck.utils.Helper

class CommentRequest(

        /* either blockID or nodeID */
        @JsonProperty("entityID")
        var entityID : String = "",

        //@JsonProperty("blockID")
        //var blockID : String = "",

        @JsonProperty("commentID")
        var commentID : String = Helper.generateId(IdentifierType.COMMENT.name),

        @JsonProperty("commentBody")
        var commentBody : AdvancedElement ?= null,

        @JsonProperty("commentedBy")
        var commentedBy : String = ""
) : WDRequest
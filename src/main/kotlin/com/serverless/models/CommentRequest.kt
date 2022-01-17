package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.models.AdvancedElement
import com.workduck.models.IdentifierType
import com.workduck.utils.Helper

class CommentRequest(

        @JsonProperty("nodeID")
        var nodeID : String = "",

        @JsonProperty("blockID")
        var blockID : String = "",

        @JsonProperty("commentID")
        var commentID : String = Helper.generateId(IdentifierType.COMMENT.name),

        @JsonProperty("commentBody")
        var commentBody : AdvancedElement ?= null,

        @JsonProperty("commentedBy")
        var commentedBy : String = ""
) : WDRequest
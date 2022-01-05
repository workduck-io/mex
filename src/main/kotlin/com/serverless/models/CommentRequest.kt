package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.models.AdvancedElement

class CommentRequest(

        @JsonProperty("nodeID")
        var nodeID : String = "",

        @JsonProperty("blockID")
        var blockID : String = "",

        @JsonProperty("commentID")
        var commentID : String = "",

        @JsonProperty("commentBody")
        var commentBody : AdvancedElement ?= null,

        @JsonProperty("commentedBy")
        var commentedBy : String = ""
) : WDRequest
package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName


@JsonTypeName("WorkspaceRequest")
class WorkspaceRequest(

        @JsonProperty("id")
        val id : String = "",

        @JsonProperty("name")
        val name : String = ""

) {
}
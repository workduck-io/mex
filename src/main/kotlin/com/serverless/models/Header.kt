package com.serverless.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Header(

   @JsonProperty("mex-workspace-id")
   val workspaceID : String,

   @JsonProperty("authorization")
   val bearerToken : String
)

package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
class TagRequest(

    val tagNames : Set<String>,

    val nodeID: String
) : WDRequest
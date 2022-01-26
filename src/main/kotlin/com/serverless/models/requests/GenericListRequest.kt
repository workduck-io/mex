package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonProperty

class GenericListRequest(
        @JsonProperty("ids")
        val ids: List<String>
) : WDRequest
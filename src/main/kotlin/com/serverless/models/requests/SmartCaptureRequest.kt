package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Messages
import com.serverless.utils.isValidCaptureID
import com.serverless.utils.isValidTitle
import com.workduck.models.AdvancedElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SmartCaptureRequest")
data class SmartCaptureRequest(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("data")
    val data: List<AdvancedElement>,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("version")
    val version: Int? = 1,

    ) : WDRequest {

    init {
        require(id.isValidCaptureID()) {
            Messages.INVALID_CAPTURE_ID
        }

        require(title.isNotEmpty()) {
            "Title is required"
        }

        require(title.isValidTitle()) {
            "Invalid Title"
        }
    }
}

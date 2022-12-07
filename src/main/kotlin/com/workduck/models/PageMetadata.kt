package com.workduck.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.Constants
import com.serverless.utils.isValidID

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageMetadata(
    var icon: Icon? = null,

    // TODO(Add validation to check if the id exists)
    var templateID: String? = null // snippet id

) {
    init {
        require(templateID?.isValidID(Constants.SNIPPET_ID_PREFIX) ?: true) { "Invalid TemplateID" }
    }
}

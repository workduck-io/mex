package com.workduck.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.extensions.isValidSnippetID

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageMetadata(
    var icon: Icon? = null,

    // TODO(Add validation to check if the id exists)
    var templateID: String? = null // snippet id

) {
    init {
        require(templateID?.isValidSnippetID() ?: true) { "Invalid TemplateID" }
    }
}

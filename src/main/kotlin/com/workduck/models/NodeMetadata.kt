package com.workduck.models

import com.serverless.utils.Constants
import com.serverless.utils.isValidID

data class NodeMetadata(
    var iconUrl: String? = null,

    var templateID: String? = null // snippet id

) {
    init {
        require(templateID?.isValidID(Constants.SNIPPET_ID_PREFIX) ?: true) { "Invalid TemplateID" }
    }
}

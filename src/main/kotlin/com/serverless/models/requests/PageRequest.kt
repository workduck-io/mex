package com.serverless.models.requests

import com.workduck.models.AdvancedElement
import com.workduck.models.NamespaceIdentifier

interface PageRequest {

    val lastEditedBy: String

    val namespaceIdentifier: NamespaceIdentifier?

    val data: List<AdvancedElement>?

}
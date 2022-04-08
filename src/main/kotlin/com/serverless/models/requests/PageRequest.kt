package com.serverless.models.requests

import com.workduck.models.AdvancedElement
import com.workduck.models.NamespaceIdentifier

interface PageRequest {

    //val namespaceIdentifier: NamespaceIdentifier?

    val title: String

    val data: List<AdvancedElement>?

}
package com.serverless.transformers

import com.serverless.models.responses.NodeElementsResponse
import com.serverless.models.responses.Response
import com.workduck.models.AdvancedElement

class NodeElementsTransformer : Transformer<Pair<String?, MutableList<AdvancedElement>>> {

    override fun transform(t: Pair<String?, MutableList<AdvancedElement>>?): Response = NodeElementsResponse(
        endCursor = t?.first,
        data = t?.second
    )
}
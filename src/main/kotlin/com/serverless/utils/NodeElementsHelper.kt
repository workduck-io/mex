package com.serverless.utils

import com.serverless.models.responses.Response
import com.serverless.transformers.NodeElementsTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.AdvancedElement


object NodeElementsHelper {

    val nodeTransformer : Transformer<Pair<String?, MutableList<AdvancedElement>>> = NodeElementsTransformer()

    fun convertToNodeElementResponse(pair: Pair<String?, MutableList<AdvancedElement>>) : Response?{
        return nodeTransformer.transform(pair)
    }
}
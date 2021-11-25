package com.serverless.utils

import com.serverless.models.Response
import com.serverless.transformers.NodeTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Node

object NodeHelper {

    val nodeTransformer : Transformer<Node> = NodeTransformer()

    fun convertNodeToNodeResponse(node : Entity?) : Response? {
        return nodeTransformer.transform(node as Node?)
    }
}
package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.serverless.transformers.Transformer
import com.workduck.models.Node
import com.workduck.service.NodeService

interface NodeStrategy {
    fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse
}

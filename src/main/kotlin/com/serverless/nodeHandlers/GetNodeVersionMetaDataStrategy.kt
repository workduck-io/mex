package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class GetNodeVersionMetaDataStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val nodeID = input.pathParameters?.id

        return if(nodeID != null) {
            val metaDataList : MutableList<String>? = NodeService().getMetaDataForActiveVersions(nodeID)
            ApiResponseHelper.generateResponseWithJsonList(metaDataList as Any?, Messages.ERROR_GETTING_NODE_VERSIONS)
        }
        else{
            ApiResponseHelper.generateResponseWithJsonList(null, Messages.ERROR_GETTING_NODE_VERSIONS)
        }
    }
}
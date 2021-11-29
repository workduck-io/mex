package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class GetNodeVersionMetaDataStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting node versions!"

        val nodeID = input.pathParameters?.id

        return if(nodeID != null) {
            val metaDataList : MutableList<String>? = NodeService().getMetaDataForActiveVersions(nodeID)
            ApiResponseHelper.generateResponseWithJsonList(metaDataList as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateResponseWithJsonList(null, errorMessage)
        }
    }
}
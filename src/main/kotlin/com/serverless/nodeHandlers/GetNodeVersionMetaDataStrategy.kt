package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.NodeService

class GetNodeVersionMetaDataStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting node versions!"

        val pathParameters = input["pathParameters"] as Map<*, *>?

        return if(pathParameters != null) {
            val nodeID = pathParameters["id"] as String
            val metaDataList : MutableList<String>? = NodeService().getMetaDataForActiveVersions(nodeID)
            ApiResponseHelper.generateResponseWithJsonList(metaDataList as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateResponseWithJsonList(null, errorMessage)
        }
    }
}
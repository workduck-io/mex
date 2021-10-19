package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.NodeService

class GetNodesByWorkspaceStrategy : NodeStrategy {
	override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
		val errorMessage = "Error getting users!"
		val pathParameters = input["pathParameters"] as Map<*, *>?
		val workspaceID = pathParameters!!["id"] as String

		val nodes : MutableList<String>? = nodeService.getAllNodesWithWorkspaceID(workspaceID)
		return ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)
	}
}
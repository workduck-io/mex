package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class UpdateNodeBlockStrategy: NodeStrategy {
	override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {

		val errorMessage = "Error updating node block"

		val json = input["body"] as String

		val pathParameters = input["pathParameters"] as Map<*, *>?
		val nodeID = pathParameters!!["id"] as String

		val node : Entity? = nodeService.updateNodeBlock(nodeID, json)
		return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)
	}
}
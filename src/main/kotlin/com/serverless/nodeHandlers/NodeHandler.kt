package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager

class NodeHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val nodeService = NodeService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val method = input["httpMethod"] as String
		val path = input["path"] as String

		if (method == "GET" && path.startsWith("/node/NODE")) {

			val errorMessage = "Error getting node"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val nodeID = pathParameters!!["id"] as String

			val node : Entity? = nodeService.getNode(nodeID)
			return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)

		} else if (method == "POST" && path == "/node") {

			val errorMessage = "Error creating node"

			val json = input["body"] as String
			val node : Entity? = nodeService.createNode(json)
			return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)

		} else if (method == "DELETE" && path.startsWith("/node/NODE")) {

			val errorMessage = "Error deleting node"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val nodeID = pathParameters!!["id"] as String

			val identifier : Identifier? = nodeService.deleteNode(nodeID)
			return ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)

		} else if (method == "POST" && path == "/node/update") {
			val errorMessage = "Error updating node"
			val json = input["body"] as String

			val node : Entity? = nodeService.updateNode(json)
			return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)

		} else if (method == "POST" && path.startsWith("/node/NODE") && path.endsWith("/append")) {

			val errorMessage = "Error appending to node!"
			val json = input["body"] as String
			val pathParameters = input["pathParameters"] as Map<*, *>?
			val nodeID = pathParameters!!["id"] as String

			val map : Map<String, Any>? = nodeService.append(nodeID, json)

			return ApiResponseHelper.generateStandardResponse(map as Any?, errorMessage)
		}
		else if (method == "GET" && path.startsWith("/node/workspace/WORKSPACE") && path.contains("/namespace/NAMESPACE")) {
			val errorMessage = "Error getting users!"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val namespaceID = pathParameters!!["namespaceID"] as String
			val workspaceID = pathParameters!!["workspaceID"] as String

			val nodes : MutableList<String>? = nodeService.getAllNodesWithNamespaceID(namespaceID, workspaceID)

			return ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)

		} else if (method == "GET" && path.startsWith("/node/workspace/WORKSPACE")) {
			val errorMessage = "Error getting users!"
			val pathParameters = input["pathParameters"] as Map<*, *>?
			val workspaceID = pathParameters!!["id"] as String

			val nodes : MutableList<String>? = nodeService.getAllNodesWithWorkspaceID(workspaceID)
			return ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)

		} else {
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}
		}

	}

	companion object {
		private val LOG = LogManager.getLogger(NodeHandler::class.java)
	}
}
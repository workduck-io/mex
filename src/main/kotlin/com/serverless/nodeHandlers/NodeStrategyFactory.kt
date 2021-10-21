package com.serverless.nodeHandlers


class NodeStrategyFactory {


	companion object {

		const val getNodeObject = "GET /node/{id}"

		const val createNodeObject = "POST /node"

		const val updateNodeObject = "POST /node/update"

		const val deleteNodeObject = "DELETE /node/{id}"

		const val appendToNodeObject = "POST /node/{id}/append"

		const val getNodesByNamespaceObject = "GET /node/workspace/{workspaceID}/namespace/{namespaceID}"

		const val getNodesByWorkspaceObject = "GET /node/workspace/{workspaceID}"

		const val updateNodeBlock = "POST /node/{id}/block/{blockIndex}/update"


		private val nodeRegistry: Map<String, NodeStrategy> = mapOf(
			getNodeObject to GetNodeStrategy(),
			createNodeObject to CreateNodeStrategy(),
			updateNodeObject to UpdateNodeStrategy(),
			deleteNodeObject to DeleteNodeStrategy(),
			appendToNodeObject to AppendToNodeStrategy(),
			getNodesByNamespaceObject to GetNodesByNamespaceStrategy(),
			getNodesByWorkspaceObject to GetNodesByWorkspaceStrategy(),
			updateNodeBlock to UpdateNodeBlockStrategy()
		)


		fun getNodeStrategy(routeKey : String): NodeStrategy? {
			return nodeRegistry[routeKey]
		}
	}


}

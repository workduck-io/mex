package com.serverless.nodeHandlers

import com.serverless.RequestObject

class NodeStrategyFactory {


	companion object {

		val getNodeObject: RequestObject = RequestObject("GET", "/node/{id}")

		val createNodeObject: RequestObject = RequestObject("POST", "/node")

		val updateNodeObject: RequestObject = RequestObject("POST", "/node/update")

		val deleteNodeObject: RequestObject = RequestObject("DELETE", "/node/{id}")

		val appendToNodeObject : RequestObject = RequestObject("POST", "/node/{id}/append")

		val getNodesByNamespaceObject : RequestObject = RequestObject("GET", "/node/workspace/{workspaceID}/namespace/{namespaceID}")

		val getNodesByWorkspaceObject : RequestObject = RequestObject("GET", "/node/workspace/{workspaceID}")

		val updateNodeBlock : RequestObject = RequestObject("POST", "/node/{id}/block/{blockIndex}/update")


		val nodeRegistry: Map<RequestObject, NodeStrategy> = mapOf(
			getNodeObject to GetNodeStrategy(),
			createNodeObject to CreateNodeStrategy(),
			updateNodeObject to UpdateNodeStrategy(),
			deleteNodeObject to DeleteNodeStrategy(),
			appendToNodeObject to AppendToNodeStrategy(),
			getNodesByNamespaceObject to GetNodesByNamespaceStrategy(),
			getNodesByWorkspaceObject to GetNodesByWorkspaceStrategy(),
			updateNodeBlock to UpdateNodeBlockStrategy()
		)


		fun getNodeStrategy(requestObject: RequestObject): NodeStrategy? {
			return nodeRegistry[requestObject]
		}
	}


}

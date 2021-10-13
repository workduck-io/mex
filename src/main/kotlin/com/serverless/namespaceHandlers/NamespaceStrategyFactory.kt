package com.serverless.namespaceHandlers

import com.serverless.RequestObject



class NamespaceStrategyFactory{

	companion object {

		val getNamespaceObject: RequestObject = RequestObject("GET", "/namespace/{id}")

		val createNamespaceObject: RequestObject = RequestObject("POST", "/namespace")

		val updateNamespaceObject: RequestObject = RequestObject("POST", "/namespace/update")

		val deleteNamespaceObject: RequestObject = RequestObject("DELETE", "/namespace/{id}")

		val getNamespaceDataObject: RequestObject = RequestObject("GET", "/namespace/data/{ids}")

		val namespaceRegistry: Map<RequestObject, NamespaceStrategy> = mapOf(
			getNamespaceObject to GetNamespaceStrategy(),
			createNamespaceObject to CreateNamespaceStrategy(),
			updateNamespaceObject to UpdateNamespaceStrategy(),
			deleteNamespaceObject to DeleteNamespaceStrategy(),
			getNamespaceDataObject to GetNamespaceDataStrategy()
		)


		fun getNamespaceStrategy(requestObject: RequestObject): NamespaceStrategy? {
			return namespaceRegistry[requestObject]
		}
	}

}



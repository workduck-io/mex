package com.serverless.userHandlers

import com.serverless.RequestObject

class UserStrategyFactory {
	companion object {

		val getUserObject: RequestObject = RequestObject("GET", "/user/{id}")

		val createUserObject: RequestObject = RequestObject("POST", "/user")

		val updateUserObject: RequestObject = RequestObject("POST", "/user/update")

		val deleteUserObject: RequestObject = RequestObject("DELETE", "/user/{id}")

		val getUsersByNamespaceObject : RequestObject = RequestObject("GET", "/user/namespace/{namespaceID}")

		val getUsersByWorkspaceObject : RequestObject = RequestObject("GET", "/user/workspace/{workspaceID}")


		val userRegistry: Map<RequestObject, UserStrategy> = mapOf(
			getUserObject to GetUserStrategy(),
			createUserObject to CreateUserStrategy(),
			updateUserObject to UpdateUserStrategy(),
			deleteUserObject to DeleteUserStrategy(),
			getUsersByNamespaceObject to GetUsersByNamespaceStrategy(),
			getUsersByWorkspaceObject to GetUsersByWorkspaceStrategy()
		)


		fun getUserStrategy(requestObject: RequestObject): UserStrategy? {
			return userRegistry[requestObject]
		}
	}

}
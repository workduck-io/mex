package com.serverless.userHandlers

class UserStrategyFactory {
    companion object {

        const val getUserObject = "GET /user/{id}"

        const val createUserObject = "POST /user"

        const val updateUserObject = "POST /user/update"

        const val deleteUserObject = "DELETE /user/{id}"

        const val getUsersByNamespaceObject = "GET /user/namespace/{namespaceID}"

        const val getUsersByWorkspaceObject = "GET /user/workspace/{workspaceID}"

        const val registerUserObject = "POST /user/register"

        private val userRegistry: Map<String, UserStrategy> = mapOf(
            getUserObject to GetUserStrategy(),
            createUserObject to CreateUserStrategy(),
            updateUserObject to UpdateUserStrategy(),
            deleteUserObject to DeleteUserStrategy(),
            getUsersByNamespaceObject to GetUsersByNamespaceStrategy(),
            getUsersByWorkspaceObject to GetUsersByWorkspaceStrategy(),
            registerUserObject to RegisterUserStrategy()
        )

        fun getUserStrategy(routeKey: String): UserStrategy? {
            return userRegistry[routeKey]
        }
    }
}

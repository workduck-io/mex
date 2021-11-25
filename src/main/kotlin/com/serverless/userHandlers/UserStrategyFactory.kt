package com.serverless.userHandlers

class UserStrategyFactory {
    companion object {

        const val registerUserObject = "POST /user/register"

        private val userRegistry: Map<String, UserStrategy> = mapOf(
            registerUserObject to RegisterUserStrategy()
        )

        fun getUserStrategy(routeKey: String): UserStrategy? {
            return userRegistry[routeKey]
        }
    }
}

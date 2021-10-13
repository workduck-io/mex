package com.serverless.userIdentifierMappingHandlers

import com.serverless.RequestObject

class UserIdentifierMappingStrategyFactory {

	companion object{
		val createUserIdentifierMappingObject : RequestObject = RequestObject("POST", "/userIdentifierMappingRecord")

		val getUserIdentifierMappingObject : RequestObject = RequestObject("GET", "/userIdentifierMappingRecord/{userID}/{identifierID}")

		val getUserRecordsObject : RequestObject = RequestObject("GET", "/userRecords/{userID}")

		val userIdentifierMappingRegistry: Map<RequestObject, UserIdentifierMappingStrategy> = mapOf(
			getUserIdentifierMappingObject to DeleteUserIdentifierMappingStrategy(),
			createUserIdentifierMappingObject to CreateUserIdentifierMappingStrategy(),
			getUserRecordsObject to GetUserRecordsStrategy()
		)

		fun getUserIdentifierMappingStrategy(requestObject: RequestObject): UserIdentifierMappingStrategy? {
			return userIdentifierMappingRegistry[requestObject]
		}
	}

}
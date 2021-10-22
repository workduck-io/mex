package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.NamespaceService

class CreateNamespaceStrategy : NamespaceStrategy {

	override fun apply(input: Map<String, Any>, namespaceService : NamespaceService): ApiGatewayResponse {
		val errorMessage = "Error creating namespace"

		val json = input["body"] as String
		val namespace : Entity? = namespaceService.createNamespace(json)

		return ApiResponseHelper.generateStandardResponse(namespace as Any?, errorMessage)
	}

}
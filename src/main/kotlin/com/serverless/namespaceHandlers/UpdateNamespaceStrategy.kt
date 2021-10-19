package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.NamespaceService

class UpdateNamespaceStrategy : NamespaceStrategy {
	override fun apply(input: Map<String, Any>, namespaceService : NamespaceService): ApiGatewayResponse {
		val errorMessage = "Error updating namespace"
		val json = input["body"] as String

		val namespace : Entity? = namespaceService.updateNamespace(json)
		return ApiResponseHelper.generateStandardResponse(namespace as Any?, errorMessage)
	}

}
package com.workduck.service


import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.WDRequest

import com.workduck.models.Entity
import com.workduck.utils.Helper

class UserService {

	fun registerUser(workspaceName: String?): Entity?{

		val jsonForWorkspaceCreation = """{
			"type": "WorkspaceRequest",
			"name": "$workspaceName"
		}"""

		val payload: WDRequest = Helper.objectMapper.readValue(jsonForWorkspaceCreation)

		return WorkspaceService().createWorkspace(payload)
	}

}

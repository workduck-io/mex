package com.serverless.workspaceHandlers

import com.serverless.RequestObject



class WorkspaceStrategyFactory{

	companion object {

		val getWorkspaceObject: RequestObject = RequestObject("GET", "/workspace/{id}")

		val createWorkspaceObject: RequestObject = RequestObject("POST", "/workspace")

		val updateWorkspaceObject: RequestObject = RequestObject("POST", "/workspace/update")

		val deleteWorkspaceObject: RequestObject = RequestObject("DELETE", "/workspace/{id}")

		val getWorkspaceDataObject: RequestObject = RequestObject("GET", "/workspace/data/{ids}")

		val workspaceRegistry: Map<RequestObject, WorkspaceStrategy> = mapOf(
			getWorkspaceObject to GetWorkspaceStrategy(),
			createWorkspaceObject to CreateWorkspaceStrategy(),
			updateWorkspaceObject to UpdateWorkspaceStrategy(),
			deleteWorkspaceObject to DeleteWorkspaceStrategy(),
			getWorkspaceDataObject to GetWorkspaceDataStrategy()
		)


		fun getWorkspaceStrategy(requestObject: RequestObject): WorkspaceStrategy? {
			return workspaceRegistry[requestObject]
		}
	}

}



package com.serverless.workspaceHandlers


class WorkspaceStrategyFactory{

	companion object {

		const val getWorkspaceObject = "GET /workspace/{id}"

		const val createWorkspaceObject = "POST /workspace"

		const val updateWorkspaceObject = "POST /workspace/update"

		const val deleteWorkspaceObject = "DELETE /workspace/{id}"

		const val getWorkspaceDataObject = "GET /workspace/data/{ids}"

		private val workspaceRegistry: Map<String, WorkspaceStrategy> = mapOf(
			getWorkspaceObject to GetWorkspaceStrategy(),
			createWorkspaceObject to CreateWorkspaceStrategy(),
			updateWorkspaceObject to UpdateWorkspaceStrategy(),
			deleteWorkspaceObject to DeleteWorkspaceStrategy(),
			getWorkspaceDataObject to GetWorkspaceDataStrategy()
		)


		fun getWorkspaceStrategy(routeKey: String): WorkspaceStrategy? {
			return workspaceRegistry[routeKey]
		}
	}

}



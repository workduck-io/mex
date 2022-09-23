package com.serverless.workspaceHandlers


class WorkspaceStrategyFactory {

    companion object {

        const val getWorkspaceObject = "GET /workspace/{id}"

        const val createWorkspaceObject = "POST /workspace"

        const val updateWorkspaceObject = "PATCH /workspace"

        const val deleteWorkspaceObject = "DELETE /workspace/{id}"

        const val getWorkspaceDataObject = "GET /workspace/data/{ids}"

        const val refreshNodeHierarchyObject = "PATCH /workspace/refreshHierarchy"

        const val registerWorkspaceObject = "POST /workspace/register"

        const val getArchivedNodeHierarchyObject = "GET /workspace/hierarchy/archived"

        private val workspaceRegistry: Map<String, WorkspaceStrategy> = mapOf(
            getWorkspaceObject to GetWorkspaceStrategy(),
            createWorkspaceObject to CreateWorkspaceStrategy(),
            updateWorkspaceObject to UpdateWorkspaceStrategy(),
            deleteWorkspaceObject to DeleteWorkspaceStrategy(),
            getWorkspaceDataObject to GetWorkspaceDataStrategy(),
            refreshNodeHierarchyObject to RefreshHierarchyStrategy(),
            registerWorkspaceObject to RegisterWorkspaceStrategy(),
            getArchivedNodeHierarchyObject to GetArchivedHierarchyStrategy()
        )

        fun getWorkspaceStrategy(routeKey: String): WorkspaceStrategy? {
            return workspaceRegistry[routeKey.replace("/v1", "")]
        }
    }
}

package com.serverless.workspaceHandlers

import com.serverless.transformers.IdentifierTransformer
import com.serverless.transformers.Transformer
import com.serverless.transformers.WorkspaceTransformer
import com.workduck.models.Identifier
import com.workduck.models.Workspace

class WorkspaceStrategyFactory {

    companion object {

        const val getWorkspaceObject = "GET /workspace/{id}"

        const val createWorkspaceObject = "POST /workspace"

        const val updateWorkspaceObject = "POST /workspace/update"

        const val deleteWorkspaceObject = "DELETE /workspace/{id}"

        const val getWorkspaceDataObject = "GET /workspace/data/{ids}"

        const val refreshNodeHierarchyObject = "PATCH /workspace/refreshHierarchy"

        const val getNodeHierarchyObject = "GET /workspace/hierarchy"

        private val workspaceRegistry: Map<String, WorkspaceStrategy> = mapOf(
            getWorkspaceObject to GetWorkspaceStrategy(),
            createWorkspaceObject to CreateWorkspaceStrategy(),
            updateWorkspaceObject to UpdateWorkspaceStrategy(),
            deleteWorkspaceObject to DeleteWorkspaceStrategy(),
            getWorkspaceDataObject to GetWorkspaceDataStrategy(),
            refreshNodeHierarchyObject to RefreshHierarchyStrategy(),
            getNodeHierarchyObject to GetHierarchyStrategy()
        )

        fun getWorkspaceStrategy(routeKey: String): WorkspaceStrategy? {
            return workspaceRegistry[routeKey]
        }
    }
}

package com.serverless.workspaceHandlers

import com.serverless.transformers.IdentifierTransformer
import com.serverless.transformers.Transformer
import com.serverless.transformers.WorkspaceTransformer
import com.workduck.models.Identifier
import com.workduck.models.Workspace

class WorkspaceStrategyFactory {

    companion object {

        val namespaceTransformer : Transformer<Workspace> = WorkspaceTransformer()

        val identifierTransformer : Transformer<Identifier> = IdentifierTransformer()

        const val getWorkspaceObject = "GET /workspace/{id}"

        const val createWorkspaceObject = "POST /workspace"

        const val updateWorkspaceObject = "POST /workspace/update"

        const val deleteWorkspaceObject = "DELETE /workspace/{id}"

        const val getWorkspaceDataObject = "GET /workspace/data/{ids}"

        private val workspaceRegistry: Map<String, WorkspaceStrategy> = mapOf(
            getWorkspaceObject to GetWorkspaceStrategy(namespaceTransformer),
            createWorkspaceObject to CreateWorkspaceStrategy(namespaceTransformer),
            updateWorkspaceObject to UpdateWorkspaceStrategy(namespaceTransformer),
            deleteWorkspaceObject to DeleteWorkspaceStrategy(identifierTransformer),
            getWorkspaceDataObject to GetWorkspaceDataStrategy(namespaceTransformer)
        )

        fun getWorkspaceStrategy(routeKey: String): WorkspaceStrategy? {
            return workspaceRegistry[routeKey]
        }
    }
}

package com.serverless.utils

import com.serverless.models.responses.Response
import com.serverless.transformers.Transformer
import com.serverless.transformers.WorkspaceTransformer
import com.workduck.models.Entity
import com.workduck.models.Workspace

object WorkspaceHelper {

    val workspaceTransformer : Transformer<Workspace> = WorkspaceTransformer()


    fun convertWorkspaceToWorkspaceResponse(workspace: Entity?) : Response? {
        return workspaceTransformer.transform(workspace as Workspace?)
    }
}
package com.serverless.nodeHandlers

import com.google.gson.Gson
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.models.Workspace
import com.workduck.service.NodeService
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager

class RefactorNodePathStrategy : NodeStrategy {
    private val LOG = LogManager.getLogger(RefactorNodePathStrategy::class.java)

    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        val workspace = WorkspaceService().getWorkspace(input.headers.workspaceID) as Workspace?
                ?: throw IllegalArgumentException(Messages.INVALID_WORKSPACE_ID)

        LOG.info("Request Payload : " + Gson().toJson(input.payload))

        LOG.info("Workspace : " + Gson().toJson(workspace))

        return input.payload?.let{ request ->
            ApiResponseHelper.generateStandardResponse(nodeService.refactor(request, input.tokenBody.userID, workspace)
                    , Messages.ERROR_UPDATING_NODE_PATH)} ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_UPDATING_NODE_PATH)

    }
}
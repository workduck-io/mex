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

        val stage =  System.getenv("STAGE")

        if(stage == "local" || stage == "dev" || stage == "test") {
            LOG.info("Request Payload : " + Gson().toJson(input.payload))
        }

        return input.payload?.let{ request ->
            ApiResponseHelper.generateStandardResponse(nodeService.refactor(request, input.tokenBody.userID, input.headers.workspaceID)
                    , Messages.ERROR_UPDATING_NODE_PATH)} ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_UPDATING_NODE_PATH)

    }
}
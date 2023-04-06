package com.serverless.smartCaptureHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getBooleanFromQueryParam
import com.serverless.utils.extensions.getConfigIDFromQueryParam
import com.workduck.service.SmartCaptureService

class GetAllSmartCapturesForFilterStrategy: SmartCaptureStrategy {
    override fun apply(input: Input, smartCaptureService: SmartCaptureService): ApiGatewayResponse {

        val (filterType, filterValue) = handleQueryParams(input)

        return smartCaptureService.getAllSmartCapturesForFilter(input.headers.workspaceID, input.tokenBody.userID, filterType, filterValue).let {
            ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_DELETING_SMART_CAPTURE)
        }
    }



    private fun handleQueryParams(input: Input) : Pair<String, String>{
        val configID = input.getConfigIDFromQueryParam()
        val workspaceID = if (input.getBooleanFromQueryParam("isWorkspaceID")) input.headers.workspaceID else null
        val userID = if (input.getBooleanFromQueryParam("isUserID")) input.tokenBody.userID else null

        val paramMap = mapOf(Constants.CONFIG_ID to configID, Constants.WORKSPACE_ID to workspaceID, Constants.USER_ID to userID)
        val nonNullParams = paramMap.filterValues { it != null }

        /* for now, we are only allowing single queryParam to be passed */
        require(nonNullParams.size == 1) { Messages.BAD_REQUEST }

        val (filterType, filterValue) = nonNullParams.entries.first()
        return filterType to (filterValue as String)

    }
}
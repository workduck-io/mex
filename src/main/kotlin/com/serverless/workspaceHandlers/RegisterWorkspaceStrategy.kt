package com.serverless.workspaceHandlers


import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.requests.RegisterWorkspaceRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Messages
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService
import com.workduck.utils.Helper

class RegisterWorkspaceStrategy: WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val registerWorkspaceRequest = (input.payload as RegisterWorkspaceRequest?) ?: throw IllegalArgumentException("Invalid Body")

        val workspaceName = registerWorkspaceRequest.workspaceName
        val workspace: Workspace = workspaceService.createWorkspace(createWorkspacePayload(workspaceName)) as Workspace? ?: return ApiResponseHelper.generateStandardErrorResponse("Unable to Create Workspace")

        try {
            initializeWorkspace(workspace.id, registerWorkspaceRequest.userID)
        } catch (e : Exception) {
            return ApiResponseHelper.generateStandardErrorResponse(e.message ?: "Initialize Workspace Lambda Unresponsive")
        }
        return ApiResponseHelper.generateStandardResponse(workspace.id, Messages.ERROR_REGISTERING_USER)
    }

    private fun createWorkspacePayload(workspaceName: String) : WDRequest{
        val jsonForWorkspaceCreation = """{
			"type": "WorkspaceRequest",
			"name": "$workspaceName"
		}"""
        return Helper.objectMapper.readValue(jsonForWorkspaceCreation)
    }

    private fun initializeWorkspace(workspaceID: String, userID: String){
        val lambdaClient = AWSLambdaClient.builder().withRegion("us-east-1").build()
        val request = InvokeRequest()
        val stage = System.getenv("STAGE")
        val functionName = "initialize-workspace-$stage-initializeWorkspace"
        val payload = """{
			"workspaceID" : "$workspaceID",
            "userID" : "$userID"
		}
		"""
        request.withFunctionName(functionName).withPayload(payload)
        val lambdaResult = lambdaClient.invoke(request)
        when (lambdaResult.statusCode) {
            200 -> return
            else -> throw Exception("Initialize Workspace Lambda Unresponsive")
        }
    }

}


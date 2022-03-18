package com.serverless.userHandlers

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest
import com.amazonaws.services.cognitoidp.model.AttributeType
import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.lambda.model.InvokeResult
import com.google.gson.Gson
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.TokenBody
import com.serverless.models.requests.RegisterUserRequest
import com.workduck.models.Workspace
import com.workduck.service.UserService
import com.workduck.utils.Helper


class RegisterUserStrategy : UserStrategy {
    override fun apply(input: Input, userService: UserService): ApiGatewayResponse {
        val errorMessage = "Error registering user"

        val registerUserRequest = (input.payload as RegisterUserRequest?) ?: throw IllegalArgumentException("Invalid Body")

        val userJson = Helper.objectMapper.writeValueAsString(registerUserRequest.user)
        val workspaceName = registerUserRequest.workspaceName

        println("USER JSON : $userJson")
        val workspace: Workspace = userService.registerUser(workspaceName) as Workspace? ?: return ApiResponseHelper.generateStandardErrorResponse("Unable to Create Workspace")

        val payload = """{
			"id" : "${registerUserRequest.user.id}",
            "name" : "${registerUserRequest.user.name}",
			"group" : "${workspace.id}",
			"tag" : "MEX",
            "properties" : $userJson
			}
		"""

        val invokeResult = updateUser(payload)
        when(invokeResult.statusCode){
            200 -> updateCognitoPool(input, workspace.id)
            else -> {
                ApiResponseHelper.generateStandardErrorResponse("Error updating user", invokeResult.statusCode)
            }
        }

        return ApiResponseHelper.generateStandardResponse(workspace as Any?, errorMessage)
    }

    private fun updateCognitoPool(input: Input, workspaceID: String){
        val tokenBody: TokenBody = TokenBody.fromToken(input.headers.bearerToken) ?: throw UnauthorizedException("Unauthorized")

        val client = AWSCognitoIdentityProviderClientBuilder.standard().build()

        println("userPoolID : ${tokenBody.userPoolID}")
        println("userName : ${tokenBody.userName}")


        val adminGetUserRequest = AdminGetUserRequest()
                .withUserPoolId(tokenBody.userPoolID)
                .withUsername(tokenBody.userName)

        val adminGetUserResult : AdminGetUserResult = client.adminGetUser(adminGetUserRequest)

        var workspaceString = ""
        for(attribute in adminGetUserResult.userAttributes){
            if(attribute.name == "custom:mex_workspace_ids")
                workspaceString = attribute.value
        }

        println("WorkspaceString: $workspaceString")


        val newAttribute = AttributeType()
        newAttribute.name = "custom:mex_workspace_ids"
        newAttribute.value = getUpdatedWorkspaceIDString(workspaceString, workspaceID)

        val adminUpdateUserAttributesRequest = AdminUpdateUserAttributesRequest()
                .withUserPoolId(tokenBody.userPoolID)
                .withUsername(tokenBody.userName)
                .withUserAttributes(newAttribute)

        client.adminUpdateUserAttributes(adminUpdateUserAttributesRequest)

        println("RESULT : ${Gson().toJson(adminGetUserResult)}")


    }

    private fun getUpdatedWorkspaceIDString(_workspaceString: String, workspaceID: String) : String{
        var workspaceString = _workspaceString
        if(workspaceString == "") workspaceString = workspaceID
        else{
            workspaceString += "#$workspaceID"
        }
        return workspaceString
    }

    private fun updateUser(payload: String): InvokeResult {
        val lambdaClient = AWSLambdaClient.builder().withRegion("us-east-1").build()

        val request = InvokeRequest()

        val stage = System.getenv("STAGE")

        val functionName = "workduck-user-service-$stage-updateUser"

        request.withFunctionName(functionName).withPayload(payload)

        return lambdaClient.invoke(request)

    }

}


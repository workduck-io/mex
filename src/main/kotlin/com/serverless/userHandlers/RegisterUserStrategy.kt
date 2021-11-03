package com.serverless.userHandlers

import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.lambda.model.InvokeResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.UserRegister
import com.workduck.models.Workspace
import com.workduck.service.UserService

class RegisterUserStrategy : UserStrategy {
    override fun apply(input: Map<String, Any>, userService: UserService): ApiGatewayResponse {
        val errorMessage = "Error registering user"
        val json = input["body"] as String

        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val obj : UserRegister = objectMapper.readValue(json)

        val userJson = objectMapper.writeValueAsString(obj.user)
        val workspaceName = obj.workspaceName

        val workspace: Workspace? = userService.registerUser(userJson, workspaceName) as Workspace?

        val workspaceID = workspace?.id


        val lambdaClient = AWSLambdaClient.builder().withRegion("us-east-1").build()

        val request = InvokeRequest()


        val payload = """{
			"id" : "${obj.user?.id}",
            "name" : "${obj.user?.name}",
			"group" : "$workspaceID",
			"tag" : "MEX",
            "properties" : $userJson
			}
		"""

        val stage =  System.getenv("STAGE")

        val functionName = "workduck-user-service-$stage-updateUser"


        request.withFunctionName(functionName).withPayload(payload)
        val invoke: InvokeResult = lambdaClient.invoke(request)

        println("Result invoking $functionName: $invoke")

        return ApiResponseHelper.generateStandardResponse(workspace as Any?, errorMessage)
    }
}

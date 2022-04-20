package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Constants
import com.serverless.utils.Helper.EMAIL_ADDRESS_PATTERN
import com.workduck.service.NodeService

class GetAllNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting nodes!"

        /*
            if by namespace : id = namespaceID-workspaceID
            if by workspace : id = workspaceID
            if by user : id = userID
         */
        val idList = input.pathParameters?.id?.split(Constants.PATH_PARAMETER_SEPARATOR)

        return if (idList != null) {
            when (idList.size) {
                1 -> when {
                        idList[0].startsWith("WORKSPACE") ->
                            nodeService.getAllNodesWithWorkspaceID(idList[0]).let {
                                ApiResponseHelper.generateStandardResponse(it as Any?, errorMessage)
                            }

                        else ->
                            nodeService.getAllNodesWithUserID(idList[0]).let {
                                ApiResponseHelper.generateStandardResponse(it as Any?, errorMessage)
                            }

                    }

                2 -> when {
                    isValidNamespaceAndWorkspaceID(idList[0], idList[1]) ->
                        nodeService.getAllNodesWithNamespaceID(idList[0], idList[1]).let {
                            ApiResponseHelper.generateStandardResponse(it as Any?, errorMessage)
                        }
                    else -> ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

                }
                else -> throw IllegalArgumentException("Invalid ID")
            }
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }

    }

    private fun checkEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    private fun isValidNamespaceAndWorkspaceID(namespaceID: String, workspaceID: String) : Boolean{
       return namespaceID.startsWith("NAMESPACE") && workspaceID.startsWith("WORKSPACE")
    }
}

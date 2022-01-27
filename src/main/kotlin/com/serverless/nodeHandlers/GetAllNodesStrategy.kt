package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class GetAllNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting nodes!"

        /*
            if by namespace : id = namespaceID#workspaceID
            if by workspace : id = workspaceID
            if by user : id = userID
         */
        val idList = input.pathParameters?.id?.split("-")

        return if (idList != null) {
            when (idList.size) {
                1 -> when {
                    isValidWorkspaceOrUserID(idList[0]) -> when {
                        idList[0].startsWith("WORKSPACE") ->
                            nodeService.getAllNodesWithWorkspaceID(idList[0]).let {
                                ApiResponseHelper.generateStandardResponse(it as Any?, errorMessage)
                            }

                        idList[0].startsWith("USER") ->
                            nodeService.getAllNodesWithUserID(idList[0]).let {
                                ApiResponseHelper.generateStandardResponse(it as Any?, errorMessage)
                            }
                        else -> ApiResponseHelper.generateStandardErrorResponse(errorMessage)
                    }
                    else -> ApiResponseHelper.generateStandardErrorResponse(errorMessage)
                }
                2 -> when {
                    isValidNamespaceAndWorkspaceID(idList[0], idList[1]) ->
                        nodeService.getAllNodesWithNamespaceID(idList[0], idList[1]).let {
                            ApiResponseHelper.generateStandardResponse(it as Any?, errorMessage)
                        }
                    else -> ApiResponseHelper.generateStandardErrorResponse(errorMessage)

                }
                else -> throw IllegalArgumentException("Invalid ID")
            }
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }

    }

    private fun isValidWorkspaceOrUserID(id : String) : Boolean{
        when(id.startsWith("WORKSPACE") || id.startsWith("USER")) {
            true -> return true
            false -> throw IllegalArgumentException("Invalid ID")
        }
    }

    private fun isValidNamespaceAndWorkspaceID(namespaceID: String, workspaceID: String) : Boolean{
        when(namespaceID.startsWith("NAMESPACE") && workspaceID.startsWith("WORKSPACE")){
            true -> return true
            false -> throw IllegalArgumentException("Invalid ID")
        }
    }
}

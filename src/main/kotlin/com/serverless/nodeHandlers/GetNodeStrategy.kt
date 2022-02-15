package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.NodeElementsHelper
import com.serverless.utils.NodeHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class GetNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting node"


        val pathParameters = input.pathParameters
        val queryStringParameters = input.queryStringParameters
        println("pathParameters : $pathParameters")
        println("queryParameters : $queryStringParameters")
        val nodeID = pathParameters?.id as String

        val bookmarkInfo = queryStringParameters?.let{
            it["bookmarkInfo"].toBoolean()
        }

        val userID = queryStringParameters?.let{
            it["userID"]
        }

        val startCursor = queryStringParameters?.let{
            it["startCursor"]
        }

        val blockSize = queryStringParameters?.let{
            it["blockSize"]?.toInt()
        }

        val getMetaDataOfNode = queryStringParameters?.let{
            it["getMetaDataOfNode"]?.toBoolean() ?: true
        } ?: true

        val getReverseOrder = queryStringParameters?.let{
            it["getReverseOrder"]?.toBoolean() ?: false
        } ?: false


        /* if blockSize is not provided, get all the node's data */
        return when(blockSize == null){

            true -> {
                val node : Entity? = nodeService.getNode(nodeID, bookmarkInfo, userID)
                val nodeResponse : Response? = NodeHelper.convertNodeToNodeResponse(node)
                ApiResponseHelper.generateStandardResponse(nodeResponse, 200, errorMessage)
            }

            false -> {
                return when(getMetaDataOfNode){
                    true -> {
                        val node: Entity? = nodeService.getNodeData(nodeID, startCursor, blockSize, getReverseOrder, bookmarkInfo, userID)
                        val nodeResponse : Response? = NodeHelper.convertNodeToNodeResponse(node)
                        ApiResponseHelper.generateStandardResponse(nodeResponse, 200, errorMessage)
                    }
                    false -> {
                        val pair = nodeService.getNodeElements(nodeID, startCursor, blockSize, getReverseOrder)
                        val response = NodeElementsHelper.convertToNodeElementResponse(pair)
                        ApiResponseHelper.generateStandardResponse(response,200,  errorMessage)

                    }
                }
            }
        }
    }
}

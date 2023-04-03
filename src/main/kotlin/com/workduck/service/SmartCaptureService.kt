package com.workduck.service

import com.amazonaws.services.lambda.model.InvocationType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.NodeNamespaceMap
import com.serverless.models.requests.NodeWorkspaceMap
import com.serverless.models.requests.SmartCaptureRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AdvancedElement
import com.workduck.models.EntityOperationType
import com.workduck.models.EntityServiceCreateResponse
import com.workduck.models.externalLambdas.ExternalRequestHeader
import com.workduck.models.externalLambdas.RequestContext
import com.workduck.utils.EntityHelper
import com.workduck.utils.Helper
import com.workduck.utils.LambdaHelper
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

class SmartCaptureService (
    private val objectMapper: ObjectMapper = Helper.objectMapper,

    private val nodeService : NodeService = NodeService()
){

    fun createSmartCapture(wdRequest: WDRequest, userID: String, userWorkspaceID: String) : String {
        val request = wdRequest as SmartCaptureRequest

        // this contains the nodeID to which smartCapture should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(request.nodeNamespaceMap, userID, userWorkspaceID)

        val smartCapture: AdvancedElement = request.data
        populateSmartCaptureMetadata(smartCapture, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)

        val captureID = invokeCreateCaptureLambda(smartCapture, nodeWorkspaceMap.workspaceID, userID).id

        val refBlock = EntityHelper.createEntityReferenceBlock(smartCapture.id, captureID, Constants.ELEMENT_SMART_CAPTURE)
        nodeService.appendEntityBlocks(nodeWorkspaceMap.nodeID, nodeWorkspaceMap.workspaceID, userID, listOf(refBlock))

        return captureID
    }

    //TODO(make sure createAt, createdBy fields retain their values in the entity store)
    fun updateSmartCapture(smartCaptureID: String, wdRequest: WDRequest, userID: String, userWorkspaceID: String) {
        val request = wdRequest as SmartCaptureRequest

        // this contains the nodeID to which smartCapture should be appended and the workspaceID of that node.
        val nodeWorkspaceMap = getNodeIDWorkspaceID(request.nodeNamespaceMap, userID, userWorkspaceID)

        val smartCapture: AdvancedElement = request.data

        //TODO(pass correct values)
        populateSmartCaptureMetadata(smartCapture, userID, createdAt = Constants.getCurrentTime(), createdBy = userID)
        invokeUpdateCaptureLambda(smartCaptureID, smartCapture, nodeWorkspaceMap.workspaceID, userID)


    }

    fun getSmartCapture(captureID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String): AdvancedElement? {

        val workspaceID = nodeService.nodeAccessService
            .checkIfUserHasAccessAndGetWorkspaceDetails(
                nodeID,
                userWorkspaceID,
                namespaceID,
                userID,
                EntityOperationType.WRITE
            ).let { workspaceDetails ->
                require(!workspaceDetails[Constants.WORKSPACE_ID].isNullOrEmpty()) { Messages.ERROR_NODE_PERMISSION }
                workspaceDetails[Constants.WORKSPACE_ID]!!
            }

        invokeGetCaptureLambda(workspaceID, userID, captureID)
        return null
    }


    fun deleteSmartCapture(captureID: String, nodeID: String, namespaceID: String, userWorkspaceID: String, userID: String) {
        val workspaceID = nodeService.nodeAccessService
            .checkIfUserHasAccessAndGetWorkspaceDetails(
                nodeID,
                userWorkspaceID,
                namespaceID,
                userID,
                EntityOperationType.WRITE
            ).let { workspaceDetails ->
                require(!workspaceDetails[Constants.WORKSPACE_ID].isNullOrEmpty()) { Messages.ERROR_NODE_PERMISSION }
                workspaceDetails[Constants.WORKSPACE_ID]!!
            }

        invokeDeleteCaptureLambda(workspaceID, userID, captureID)
    }

    fun getAllSmartCapturesForFilter(workspaceID: String, userID: String, filterType: String, filterValue: String) {
        invokeGetCapturesWithFilterLambda(workspaceID, userID, filterType, filterValue)

    }


    private fun getNodeIDWorkspaceID(nodeNamespaceMap: NodeNamespaceMap?, userID: String, userWorkspaceID: String) : NodeWorkspaceMap {

        return when(nodeNamespaceMap == null) {
            true -> { /* if no node,namespace is given we will add the smart capture to user's workspace in default place. */
                NodeWorkspaceMap(
                    nodeID = Constants.SMART_CAPTURE_DEFAULT_NODE_ID,
                    workspaceID = Constants.WORKSPACE_ID
                )
            }

            false -> {
                val nodeID = nodeNamespaceMap.nodeID
                val namespaceID = nodeNamespaceMap.namespaceID
                val workspaceID = nodeService.nodeAccessService
                    .checkIfUserHasAccessAndGetWorkspaceDetails(
                        nodeID,
                        userWorkspaceID,
                        namespaceID,
                        userID,
                        EntityOperationType.WRITE
                    ).let { workspaceDetails ->
                        require(!workspaceDetails[Constants.WORKSPACE_ID].isNullOrEmpty()) { Messages.ERROR_NODE_PERMISSION }
                        workspaceDetails[Constants.WORKSPACE_ID]!!
                    }
                NodeWorkspaceMap(
                    nodeID = nodeID,
                    workspaceID = workspaceID
                )

            }
        }

    }


    private fun invokeCreateCaptureLambda(smartCapture: AdvancedElement, workspaceID: String, userID: String) : EntityServiceCreateResponse{
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.CREATE_CAPTURE, HttpMethods.POST)
        val requestBody = objectMapper.writeValueAsString(EntityHelper.createEntityPayload(smartCapture))
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, requestBody = requestBody)

        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)

    }

    private fun invokeUpdateCaptureLambda(captureID: String, smartCapture: AdvancedElement, workspaceID: String, userID: String){
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.CREATE_CAPTURE, HttpMethods.POST)
        val requestBody = objectMapper.writeValueAsString(EntityHelper.createEntityPayload(smartCapture))
        val pathParameters : Map<String, String> = mapOf("captureID" to captureID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, requestBody = requestBody, pathParameters = pathParameters)
    }

    private fun invokeGetCaptureLambda(workspaceID: String, userID: String, captureID: String) : AdvancedElement{
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.GET_CAPTURE, HttpMethods.GET)
        val pathParameters : Map<String, String> = mapOf("captureId" to captureID)
        val response = LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, pathParameters = pathParameters)
        val jsonBody = response.body ?: throw IllegalStateException("Could not get a response")
        return Helper.objectMapper.readValue(jsonBody)

    }

    private fun invokeDeleteCaptureLambda(workspaceID: String, userID: String, captureID: String){
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.DELETE_CAPTURE, HttpMethods.DELETE)
        val pathParameters : Map<String, String> = mapOf("captureID" to captureID)
        //TODO(invocationType could be event)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, pathParameters = pathParameters)
    }

    private fun invokeGetCapturesWithFilterLambda(workspaceID: String, userID: String, filterType: String, filterValue: String){
        val header = ExternalRequestHeader(workspaceID, userID)
        val requestContext = RequestContext(RoutePaths.GET_ALL_CAPTURES_WITH_FILTER, HttpMethods.GET)
        val queryStringParameters : Map<String, String> = mapOf("filterType" to filterType, "filterValue" to filterValue)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, queryStringParameters = queryStringParameters)
    }


    private fun populateSmartCaptureMetadata(smartCapture: AdvancedElement, userID: String, createdAt : Long?, createdBy : String?){
        smartCapture.createdBy = createdBy
        smartCapture.createdAt = createdAt
        smartCapture.lastEditedBy = userID
        smartCapture.updatedAt = createdAt?: Constants.getCurrentTime()

    }

}

fun main(){

    val bearerToken = "eyJraWQiOiJ1MjhPdDR1R1pWTGpBaXRZbUxXRm9valVQNWxtVlVmZXhiV1wvT1ZKenJUWT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI1YjJiYzljZi02Y2M4LTQwZmYtOGE1My00NWIxZWI4NTE1NjEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLWVhc3QtMS5hbWF6b25hd3MuY29tXC91cy1lYXN0LTFfWnU3RkFoN2hqIiwiY29nbml0bzp1c2VybmFtZSI6InJpc2hpdmlrcmFtK2xpbWl0QHdvcmtkdWNrLmlvIiwiY3VzdG9tOm1leF93b3Jrc3BhY2VfaWRzIjoiV09SS1NQQUNFX1ZQRG1WY0hZeEFyQ2FxR0hMbU15UiIsIm9yaWdpbl9qdGkiOiJiMjMyOGQ4Mi1jN2E2LTQ5OTMtOTE1YS03MTkyOTgyM2I2MDEiLCJhdWQiOiI2cHZxdDY0cDBsMmtxa2sycWFmZ2RoMTNxZSIsImV2ZW50X2lkIjoiNTZjNGU2NzAtZWJkNC00NGI4LTk0NDctY2ViMGNmMGU0YTQ1IiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NzMzNDIwNzIsImV4cCI6MTY3MzM0NTY3MiwiY3VzdG9tOnVzZXJfdHlwZSI6InByb21wdF93ZWJhcHAiLCJpYXQiOjE2NzMzNDIwNzIsImp0aSI6ImJlODAyNzc2LWJiYjMtNGE5OC05YzY4LWEzOTNkMjRjY2M5MyIsImVtYWlsIjoicmlzaGl2aWtyYW0rbGltaXRAd29ya2R1Y2suaW8ifQ.g4L67TMU2bhNt2gR8IivHvtCa3rRJqWKpWr-pzuAaf_FYVhLri75XpmOb2m3fkOP5aWNC2gs4Lbn2U8bj3_at7ClIpPBacIAILZoKXTJAq6LDIBF9qPvTOdIZjN_ONeO4J9H82ddSVFFfGYAk_ln8oXO7xwtW6-1Vhonuo32DGHmPWpNhBXL3EvjkxTPB15PjvRTe8u_BHKZq0xFOKaH_wznArdQa_NZAACdob-0_ICq_Ui8G1bp7_gFuJ93RQ1B1WHA2HyWbz65_94fUPd-rC00dPp5XNgUHEWl5-XNRMePKAIaUj437DPqMHN96VoSIT6pnlPu8ep6NMD-9lPCFw'"
    val json = """
        {
          "type" : "SmartCaptureRequest",
          "nodeNamespaceMap": {
            "nodeID": "NODE_4yDN4rLMqeYERAPhPDp7p",
            "namespaceID": "NAMESPACE_UNffmecrLRejbiccyVPhz"
          },
          "data": 
            {
              "id": "TEMP_tgVBX",
              "elementType": "smartCapture",
              "content": "",
              "elementMetadata": {
                "type": "smartCapture",
                "page": "LinkedIn",
                "sourceUrl": "https://www.linkedin.com/in/rishivikram-nandakumar/",
                "configID": "CONFIG_dummy123"
              },
              "children": [
                {
                  "id": "LABEL_38CJ",
                  "children": [
                    {
                      "properties": {
                        "label": "Location",
                        "value": "Bangalore Urban, Karnataka, India"
                      }
                    }
                  ],
                  "elementType": "p",
                  "properties": {
                    "type": "p",
                    "row": 0
                  }
                },
                {
                  "id": "LABEL_37Xb",
                  "children": [
                    {
                      "properties": {
                        "label": "Headline",
                        "value": "Product Engineer @Workduck Humans | Software Engineer"
                      }
                    }
                  ],
                  "elementType": "p",
                  "properties": {
                    "type": "p",
                    "row": 0,
                    "col": 1
                  }
                }
              ]
            }
          
        }
    """.trimIndent()

    val r = Helper.objectMapper.readValue<SmartCaptureRequest>(json)
    //SmartCaptureService().createSmartCapture(r, "45135611-f861-4de2-9e1f-782e4c69ec3b", "WORKSPACE_ynJdV4zBmtixbNkEYqbCB")
    SmartCaptureService().getSmartCapture("CAPTURE_K9zWcgMjrnJdytDwgtYmB", "NODE_4yDN4rLMqeYERAPhPDp7p", "NAMESPACE_UNffmecrLRejbiccyVPhz",
    "WORKSPACE_ynJdV4zBmtixbNkEYqbCB", "45135611-f861-4de2-9e1f-782e4c69ec3b")


}
package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.lambda.model.InvocationType
import com.serverless.models.Header
import com.serverless.models.requests.SmartCaptureRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.SmartCaptureHelper
import com.workduck.models.CaptureEntity
import com.workduck.models.SmartCapture
import com.workduck.models.exceptions.WDNotFoundException
import com.workduck.models.externalLambdas.RequestContext
import com.workduck.repositories.SmartCaptureRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.LambdaHelper
import com.workduck.utils.extensions.createSmartCaptureObjectFromSmartCaptureRequest
import com.workduck.utils.externalLambdas.HttpMethods
import com.workduck.utils.externalLambdas.LambdaFunctionNames
import com.workduck.utils.externalLambdas.RoutePaths

class SmartCaptureService {
    private val objectMapper = Helper.objectMapper
    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)

    private val tableName: String = DDBHelper.getTableName()

    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()

    private val smartCaptureRepository = SmartCaptureRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName)



    fun createSmartCapture(wdRequest: WDRequest, userID: String, workspaceID: String, bearerToken: String): CaptureEntity {
        val request = wdRequest as SmartCaptureRequest
        val smartCapture: SmartCapture = request.createSmartCaptureObjectFromSmartCaptureRequest(userID, workspaceID)
        setMetadata(smartCapture)

        smartCaptureRepository.createSmartCapture(smartCapture)

        val capture = SmartCaptureHelper.serializeRequestToEntity(request)
        invokeCreateCaptureLambda(capture, workspaceID, bearerToken)

        return capture
    }


    fun getSmartCapture(captureID: String, workspaceID: String, bearerToken: String): CaptureEntity? {
        val smartCapture = smartCaptureRepository.getSmartCapture(captureID, workspaceID)
            ?: throw WDNotFoundException("Requested Entity Not Found")

        val configID = smartCapture.data?.get(0)?.configID.toString()

        invokeGetCaptureLambda(workspaceID, bearerToken, captureID, configID)
        return null
    }



    private fun invokeCreateCaptureLambda(captureEntity: CaptureEntity, workspaceID: String, bearerToken: String){
        val header = Header(workspaceID, bearerToken)
        val requestContext = RequestContext(RoutePaths.CREATE_CAPTURE, HttpMethods.POST)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, requestBody = objectMapper.writeValueAsString(captureEntity))
    }

    private fun invokeGetCaptureLambda(workspaceID: String, bearerToken: String, captureID: String, configID: String){
        val header = Header(workspaceID, bearerToken)
        val requestContext = RequestContext(RoutePaths.GET_CAPTURE, HttpMethods.GET)
        val pathParameters : Map<String, String> = mapOf("captureID" to captureID, "configID" to configID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, pathParameters = pathParameters)
    }

    private fun setMetadata(smartCapture: SmartCapture){
        smartCapture.createdBy = smartCapture.lastEditedBy /* if an update operation is expected, this field will be set to null */

        if(smartCapture.data.isNullOrEmpty()) return
    }
}
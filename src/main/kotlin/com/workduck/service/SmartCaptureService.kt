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
import com.workduck.models.exceptions.WDException
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


    fun deleteSmartCapture(captureID: String, workspaceID: String, bearerToken: String) {
        val smartCapture = smartCaptureRepository.getSmartCapture(captureID, workspaceID)
            ?: throw WDNotFoundException("Smart capture does not exist")

        smartCaptureRepository.deleteSmartCapture(captureID, workspaceID)

        val configID = smartCapture.data?.get(0)?.configID.toString()
        invokeDeleteCaptureLambda(workspaceID, bearerToken, captureID, configID)
        //TODO(figure out the return because when deleting, invocationType will be event)

    }

    fun getSmartCaptureWithConfigId(configID: String, workspaceID: String, bearerToken: String) : List<CaptureEntity> {
        invokeGetCapturesWithConfigLambda(workspaceID, bearerToken, configID)
        //TODO(figure out the return)
        /*
        val allCaptures = Helper.objectMapper.readValue(response.body, Array<CaptureEntity>::class.java).toMutableList()

        for (capture in allCaptures) {
            val captureInMexRepo = smartCaptureRepository.getSmartCapture(capture.captureId.toString(), workspaceID)
            if (captureInMexRepo == null) {
                allCaptures.remove(capture)
            }
        }

        if(response.statusCode == 200) return allCaptures.toTypedArray() else return null*/
        return listOf()
    }

    fun getAllSmartCaptureForUser(configID: String, workspaceID: String, bearerToken: String) : List<CaptureEntity> {
        //TODO(why are we not passing userID?)
        invokeGetCapturesForUserLambda(workspaceID, bearerToken, configID)
        //TODO(figure out the return)

        /*
        val lambdaPayload = LambdaPayload(
            path = RoutePaths.GET_ALL_CAPTURES_FOR_USER,
            httpMethod = HttpMethods.GET,
            headers = Header(workspaceID = workspaceID, bearerToken),
            requestContext = RequestContext(resourcePath = RoutePaths.GET_ALL_CAPTURES_FOR_USER, httpMethod = HttpMethods.GET),
            routeKey = "${HttpMethods.GET} ${RoutePaths.GET_ALL_CAPTURES_FOR_USER}",
            pathParameters = mapOf("configId" to configId)
        )
        val response = Helper.invokeLambda(objectMapper.writeValueAsString(lambdaPayload), LambdaFunctionNames.CAPTURE_LAMBDA)
        val allCaptures = Helper.objectMapper.readValue(response.body, Array<CaptureEntity>::class.java).toMutableList()

        for (capture in allCaptures) {
            val captureInMexRepo = smartCaptureRepository.getSmartCapture(capture.captureId.toString(), workspaceID)
            if (captureInMexRepo == null) {
                allCaptures.remove(capture)
            }
        }

        if(response.statusCode == 200) return allCaptures.toTypedArray() else return null*/
        return listOf()
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

    private fun invokeDeleteCaptureLambda(workspaceID: String, bearerToken: String, captureID: String, configID: String){
        val header = Header(workspaceID, bearerToken)
        val requestContext = RequestContext(RoutePaths.DELETE_CAPTURE, HttpMethods.DELETE)
        val pathParameters : Map<String, String> = mapOf("captureID" to captureID, "configID" to configID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.Event, LambdaFunctionNames.CAPTURE_LAMBDA, pathParameters = pathParameters)
    }

    private fun invokeGetCapturesWithConfigLambda(workspaceID: String, bearerToken: String, configID: String){
        val header = Header(workspaceID, bearerToken)
        val requestContext = RequestContext(RoutePaths.GET_ALL_CAPTURES_WITH_CONFIGID, HttpMethods.GET)
        val pathParameters : Map<String, String> = mapOf("configId" to configID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, pathParameters = pathParameters)
    }

    private fun invokeGetCapturesForUserLambda(workspaceID: String, bearerToken: String, configID: String){
        val header = Header(workspaceID, bearerToken)
        val requestContext = RequestContext(RoutePaths.GET_ALL_CAPTURES_FOR_USER, HttpMethods.GET)
        val pathParameters : Map<String, String> = mapOf("configId" to configID)
        LambdaHelper.invokeLambda(header, requestContext, InvocationType.RequestResponse, LambdaFunctionNames.CAPTURE_LAMBDA, pathParameters = pathParameters)

    }

    private fun setMetadata(smartCapture: SmartCapture){
        smartCapture.createdBy = smartCapture.lastEditedBy /* if an update operation is expected, this field will be set to null */

        if(smartCapture.data.isNullOrEmpty()) return
    }
}
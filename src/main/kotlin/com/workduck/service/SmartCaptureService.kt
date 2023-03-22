package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.Header
import com.serverless.models.requests.SmartCaptureRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.SmartCaptureHelper
import com.serverless.utils.withNotFoundException
import com.workduck.models.CaptureEntity
import com.workduck.models.SmartCapture
import com.workduck.models.exceptions.WDNotFoundException
import com.workduck.repositories.*
import com.workduck.utils.DDBHelper
import com.workduck.utils.ExternalLambdas.*
import com.workduck.utils.Helper
import com.workduck.utils.extensions.createSmartCaptureObjectFromSmartCaptureRequest

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
    private val pageRepository: PageRepository<SmartCapture> = PageRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName)
    //private val repository: Repository<SmartCapture> = RepositoryImpl(dynamoDB, mapper, pageRepository, dynamoDBMapperConfig)


    fun createSmartCapture(wdRequest: WDRequest, userID: String, workspaceID: String, bearerToken: String): CaptureEntity {
        val request = wdRequest as SmartCaptureRequest
        val smartCapture: SmartCapture = request.createSmartCaptureObjectFromSmartCaptureRequest(userID, workspaceID)
        setMetadata(smartCapture)
        smartCaptureRepository.createSmartCapture(smartCapture)

        // Invoke the capture lambda
        val capture = SmartCaptureHelper.serializeRequestToEntity(request)
        val payload = LambdaPayload(
            body = objectMapper.writeValueAsString(capture),
            path = RoutePaths.CREATE_CAPTURE,
            httpMethod = HttpMethods.POST,
            headers = Header(workspaceID = smartCapture.workspaceIdentifier.id, bearerToken),
            requestContext = RequestContext(resourcePath = RoutePaths.CREATE_CAPTURE, httpMethod = HttpMethods.POST),
            routeKey = "${HttpMethods.POST} ${RoutePaths.CREATE_CAPTURE}"
            )
        Helper.invokeLambda(objectMapper.writeValueAsString(payload), LambdaFunctionNames.CAPTURE_LAMBDA)
        return capture
    }

    fun getSmartCapture(captureID: String, workspaceID: String, bearerToken: String): CaptureEntity? {
        val smartCapture = smartCaptureRepository.getSmartCapture(captureID, workspaceID)
            ?: throw WDNotFoundException("Requested Entity Not Found")

        val configID = smartCapture.data?.get(0)?.configId.toString()
        val lambdaPayload = LambdaPayload(
            path = RoutePaths.GET_CAPTURE,
            httpMethod = HttpMethods.GET,
            headers = Header(workspaceID = smartCapture.workspaceIdentifier.id, bearerToken),
            requestContext = RequestContext(resourcePath = RoutePaths.GET_CAPTURE, httpMethod = HttpMethods.GET),
            routeKey = "${HttpMethods.GET} ${RoutePaths.GET_CAPTURE}",
            pathParameters = mapOf("captureId" to captureID, "configId" to configID)
        )
        val result = Helper.invokeLambda(objectMapper.writeValueAsString(lambdaPayload), LambdaFunctionNames.CAPTURE_LAMBDA)
        return null
    }

    private fun setMetadata(smartCapture: SmartCapture){
        smartCapture.createdBy = smartCapture.lastEditedBy /* if an update operation is expected, this field will be set to null */

        if(smartCapture.data.isNullOrEmpty()) return
    }
}
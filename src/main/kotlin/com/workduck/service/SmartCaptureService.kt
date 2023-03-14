package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.SmartCaptureRequest
import com.serverless.models.requests.WDRequest
import com.serverless.models.responses.CaptureEntity
import com.serverless.utils.SmartCaptureHelper
import com.workduck.models.SmartCapture
import com.workduck.repositories.*
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.extensions.createSmartCaptureObjectFromSmartCaptureRequest

class SmartCaptureService {
    private val objectMapper = Helper.objectMapper
    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()
    private val smartCaptureRepository = SmartCaptureRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName)
    private val pageRepository: PageRepository<SmartCapture> = PageRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName)
    private val repository: Repository<SmartCapture> = RepositoryImpl(dynamoDB, mapper, pageRepository, dynamoDBMapperConfig)

    fun createSmartCapture(wdRequest: WDRequest, userID: String, workspaceID: String): CaptureEntity {
        val request = wdRequest as SmartCaptureRequest
        val smartCapture: SmartCapture = request.createSmartCaptureObjectFromSmartCaptureRequest(userID, workspaceID)
        setMetadata(smartCapture)
        // TODO call the entity lambda for capture creation
        println("Before")
        val cap = SmartCaptureHelper.serializeRequestToEntity(request)
        println(objectMapper.writeValueAsString(cap))
        println("done 1st phase")
        println("after")
        smartCaptureRepository.createSmartCapture(smartCapture)
        return cap
    }

    private fun setMetadata(smartCapture: SmartCapture){
        smartCapture.createdBy = smartCapture.lastEditedBy /* if an update operation is expected, this field will be set to null */

        if(smartCapture.data.isNullOrEmpty()) return
    }
}
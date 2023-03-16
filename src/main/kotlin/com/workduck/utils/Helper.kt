package com.workduck.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.logging.log4j.LogManager
import java.security.SecureRandom
import java.util.*
import com.serverless.utils.Constants


object Helper {

    private val LOG = LogManager.getLogger(Helper::class.java)

    val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).setSerializationInclusion(JsonInclude.Include.NON_NULL)

    private fun uuidBase32(prefix: String): String? {
        return uuidBase32(UUID.randomUUID(), java.lang.StringBuilder(prefix)).toString()
    }

    private fun uuidBase32(uuid: UUID, builder: StringBuilder): StringBuilder? {
        Base32.encode(uuid.mostSignificantBits, builder)
        Base32.encode(uuid.leastSignificantBits, builder)
        return builder
    }

    fun generateId(prefix: String?): String {
        return uuidBase32(UUID.randomUUID(), StringBuilder(prefix)).toString()
    }


    fun generateNanoID(prefix: String, separator: String = Constants.ID_SEPARATOR): String =
        "${prefix}$separator${NanoIdUtils.randomNanoId(SecureRandom(), Constants.NANO_ID_RANGE, Constants.NANO_ID_SIZE)}"

    fun generateNanoIDCustomLength(prefix: String, length: Int, separator: String = Constants.ID_SEPARATOR): String =
            "${prefix}$separator${NanoIdUtils.randomNanoId(SecureRandom(), Constants.NANO_ID_RANGE, length)}"

    fun isSourceWarmup(source: String?): Boolean {
        return "serverless-plugin-warmup" == source
    }


    fun validateWorkspace(workspaceID: String, workspaceIDList: List<String>): Boolean {
        return workspaceID in workspaceIDList || Constants.INTERNAL_WORKSPACE in workspaceIDList
    }

    fun logFailureForBatchOperation(failedBatches: MutableList<DynamoDBMapper.FailedBatch>) {

        for (batch in failedBatches) {
            LOG.debug("Failed to create some items: " + batch.exception)
            val items = batch.unprocessedItems
            for (entry in items) {
                for (request in entry.value) {
                    val key = request.putRequest.item
                    LOG.debug(key)
                }
            }
        }
    }


    fun getTTLForArchiving() : Long{
        return Constants.getCurrentTimeInSeconds() + 60*24*60*60 /* number of days * hours in a day *  minutes in hour * seconds in hour */
    }

    fun getTTLForDeletingNode() : Long{
        return Constants.getCurrentTimeInSeconds() + 10*24*60*60 /* number of days * hours in a day *  minutes in hour * seconds in hour */
    }


    fun getTTLForNamespace() : Long{
        return Constants.getCurrentTimeInSeconds() + 61*24*60*60 /* number of days * hours in a day *  minutes in hour * seconds in hour */
    }


    fun mapToJson(keyValueMap: Map<String, AttributeValue>): Map<String, Any?> {
        val finalKeyValueMap: MutableMap<String, Any?> = mutableMapOf()
        for ((key, value) in keyValueMap.entries) {
            when (true) {
                (value.n != null) -> finalKeyValueMap[key] = value.n
                (value.m != null) -> finalKeyValueMap[key] = mapToJson(value.m)
                (value.isNULL) -> finalKeyValueMap[key] = null
                (value.s != null) -> finalKeyValueMap[key] = value.s
                (value.bool != null) -> finalKeyValueMap[key] = value.bool
                (value.l != null) -> {
                    if(value.l.isEmpty()){
                        finalKeyValueMap[key] = emptyList<String>()
                    }else if(value.l[0].s != null) {
                        val mutableList = mutableListOf<String>()
                        for (listValue in value.l) {
                            mutableList.add(listValue.s)
                        }
                        finalKeyValueMap[key] = mutableList
                    } else if(value.l[0].m !=null) {
                        val mutableList = mutableListOf<MutableMap<String, String>>()
                        val map = mutableMapOf<String, String>()
                        for (listValue in value.l) {
                            for ((k, v) in listValue.m)
                                map[k] = v.s
                        }
                        mutableList.add(map)
                        finalKeyValueMap[key] = mutableList
                    }
                }
                else -> {
                    LOG.error("Unhandled value type $key  $value")
                    throw Error("Unhandled value type")
                }
            }
        }
        return finalKeyValueMap
    }

    fun invokeLambda(payload: String, functionName: String) {
        val lambdaClient = AWSLambdaClient.builder()
            .withRegion(Regions.US_EAST_1)
            .build()

        val res = lambdaClient.invoke(InvokeRequest()
            .withFunctionName(functionName)
            .withInvocationType(InvocationType.RequestResponse)
            .withPayload(payload.trimIndent()))

        println(res.statusCode)
        println(String(res.payload.array()))
    }








}
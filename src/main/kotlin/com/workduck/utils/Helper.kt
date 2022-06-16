package com.workduck.utils

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.logging.log4j.LogManager
import java.security.SecureRandom
import java.util.*
import com.serverless.utils.Constants


object Helper {

    private val LOG = LogManager.getLogger(Helper::class.java)

    val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

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
}

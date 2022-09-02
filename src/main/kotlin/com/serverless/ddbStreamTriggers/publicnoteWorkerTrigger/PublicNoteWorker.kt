package com.serverless.ddbStreamTriggers.publicnoteWorkerTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager
import com.fasterxml.jackson.module.kotlin.readValue


// PublicNoteWorker Lambda is triggered via DDB Streams attached to the node entity.
class PublicNoteWorker : RequestHandler<DynamodbEvent, Void> {
    private val defaultPublicNoteCacheEndpoint: String = "mex-public-note-cache.m6edlo.ng.0001.use1.cache.amazonaws.com"
    private val cacheExpTimeInSeconds: Long = 900
    private val publicNodeCache: Cache = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: defaultPublicNoteCacheEndpoint)
    private val dlqURL = System.getenv("SQS_QUEUE_URL")
    private val sqs = AmazonSQSClientBuilder.defaultClient()
    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        if(dynamodbEvent == null || dynamodbEvent.records == null) return null /* will be the case when warmup lambda calls it */

        for (record in dynamodbEvent.records) {
            val newImage = record.dynamodb.newImage
            val nodeID = newImage["SK"]?.s ?: throw Exception("Invalid Record. NodeID not available")
            val jsonResult = Helper.mapToJson(newImage).toMutableMap()
            val nodeObject : Node = Helper.objectMapper.convertValue(jsonResult, Node::class.java)
            try {
                // Check for public access update for the node
                if(nodeObject.publicAccess) {
                    val existingPublicNote = publicNodeCache.get(nodeID)
                    if(existingPublicNote != null) {
                        val existingNode : Node = Helper.objectMapper.readValue(existingPublicNote)
                        if(existingNode.updatedAt < nodeObject.updatedAt)
                            publicNodeCache.set(nodeID, cacheExpTimeInSeconds, Helper.objectMapper.writeValueAsString(jsonResult))
                    } else {
                        publicNodeCache.set(nodeID, cacheExpTimeInSeconds, Helper.objectMapper.writeValueAsString(jsonResult))
                    }
                }
                publicNodeCache.closeConnection()
            } catch (ex: Exception) {
                LOG.error(ex.message.toString())
                val sendMsgRequest = SendMessageRequest()
                                    .withQueueUrl(dlqURL)
                                    .withMessageBody(jsonResult.toString())
                sqs.sendMessage(sendMsgRequest)
            }
        }
        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteWorker::class.java)
    }
}
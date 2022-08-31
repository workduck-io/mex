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


// PublicNoteWorker Lambda is triggered via DDB Streams attached to the node entity.
class PublicNoteWorker : RequestHandler<DynamodbEvent, Void> {
    private val defaultPublicNoteCacheEndpoint: String = "mex-public-note-cache.m6edlo.ng.0001.use1.cache.amazonaws.com"
    private val cacheExpTimeInSeconds: Long = 900
    val publicNodeCache: Cache = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: defaultPublicNoteCacheEndpoint)

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
         val dlqURL = System.getenv("SQS_QUEUE_URL")
         val sqs = AmazonSQSClientBuilder.defaultClient()

            dynamodbEvent?.records?.let{
                for (record in dynamodbEvent.records) {
                    val newImage = record.dynamodb.newImage
                    LOG.debug(newImage.toString())
                    val nodeID = newImage["SK"]?.s ?: throw Exception("Invalid Record. NodeID not available")
                    val publicAccess = newImage["publicAccess"]?.n ?: throw Exception("Invalid Record. PublicAccess not available")
                    LOG.debug(publicAccess.toString())
                    // Check for public access update for the node
                    if(publicAccess == "1") {
                        val jsonResult = Helper.mapToJson(newImage).toMutableMap()
                        LOG.debug("jsonresuikt ${Helper.objectMapper.writeValueAsString(jsonResult)}")
                        try {
                            val nodeObject : Node = Helper.objectMapper.convertValue(jsonResult, Node::class.java)
                            val existingPublicNote = publicNodeCache.get(nodeID.toString())

                            LOG.debug("existingnote ${existingPublicNote.toString()}")

                            if(existingPublicNote != null) {
                                val existingNodeObject : Node = Helper.objectMapper.convertValue(existingPublicNote, Node::class.java)
                                if(existingNodeObject.updatedAt < nodeObject.updatedAt)
                                    publicNodeCache.set(nodeID.toString(), cacheExpTimeInSeconds, nodeObject.toString())
                            } else {
                                LOG.debug("before set")
                                publicNodeCache.set(nodeID.toString(), cacheExpTimeInSeconds, nodeObject.toString())
                                LOG.debug("after set")
                            }
                        } catch (ex: Exception) {
                            LOG.error(ex)
                            LOG.info("Sending to dead letter queue")
                            LOG.debug("josnResult ${jsonResult.toString()}")
                            LOG.debug("queueurl ${dlqURL}")
                            val sendMsgRequest = SendMessageRequest()
                                .withQueueUrl(dlqURL)
                                .withMessageBody(jsonResult.toString())
                            LOG.debug("sendMsgRequest.toString()")
                            sqs.sendMessage(sendMsgRequest)
                        }
                    }
                }
            }
        publicNodeCache.closeConnection()
        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteWorker::class.java)
    }
}
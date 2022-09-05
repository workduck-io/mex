package com.serverless.ddbStreamTriggers.publicnoteWorkerTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager
import com.fasterxml.jackson.module.kotlin.readValue
import javax.print.attribute.Attribute


// PublicNoteWorker Lambda is triggered via DDB Streams attached to the node entity.
class PublicNoteWorker : RequestHandler<DynamodbEvent, Void> {

    companion object {
        private const val defaultPublicNoteCacheEndpoint: String =
            "mex-public-note-cache.m6edlo.ng.0001.use1.cache.amazonaws.com"
        private const val cacheExpTimeInSeconds: Long = 900
        private val publicNodeCache: Cache =
            Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: defaultPublicNoteCacheEndpoint)
        private val dlqURL = System.getenv("SQS_QUEUE_URL")
        private val sqs = AmazonSQSClientBuilder.defaultClient()
        private val LOG = LogManager.getLogger(PublicNoteWorker::class.java)
    }

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        dynamodbEvent?.also { event ->
            event.records?.let { records ->
                records.parallelStream().map { record ->
                    val newImage = record.dynamodb.newImage
                    val node: Node = newImage.toNode()

                    try {
                        takeIf { node.hasPublicAccess() }.apply {
                            //checked for value existing in cache
                            publicNodeCache.get(node.id)?.toNode()
                                ?.also { existingNode ->
                                    if (existingNode.isOlderVariant(node)) {
                                        publicNodeCache.set(
                                            node.id,
                                            cacheExpTimeInSeconds,
                                            Helper.objectMapper.writeValueAsString(node)
                                        )
                                    }
                                } ?: publicNodeCache.set(
                                node.id,
                                cacheExpTimeInSeconds,
                                Helper.objectMapper.writeValueAsString(node)
                            )

                        }

                    } catch (ex: Exception) {
                        LOG.error(ex.message.toString())
                        val sendMsgRequest = SendMessageRequest()
                            .withQueueUrl(dlqURL)
                            .withMessageBody(node.toString())
                        sqs.sendMessage(sendMsgRequest)

                    } finally {
                        publicNodeCache.closeConnection()
                    }
                }
            }
        }
        return null
    }
}

private fun Map<String, AttributeValue>.toNode(): Node = this.also {
    require(this["SK"]?.s != null) {
        "Invalid Record. NodeID not available"
    }
}.let {
    Helper.mapToJson(this)
        .toMutableMap()
        .let { res ->
            Helper.objectMapper.convertValue(res, Node::class.java)
        }
}

private fun String.toNode(): Node = Helper.objectMapper.readValue(this)

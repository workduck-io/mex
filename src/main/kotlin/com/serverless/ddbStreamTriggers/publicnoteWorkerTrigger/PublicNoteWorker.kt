package com.serverless.ddbStreamTriggers.publicnoteWorkerTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.serverless.utils.Constants
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager


// PublicNoteWorker Lambda is triggered via DDB Streams attached to the node entity.
class PublicNoteWorker : RequestHandler<DynamodbEvent, Void> {

    companion object {
        private val publicNodeCache: Cache<Node> =
            Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: Constants.defaultPublicNoteCacheEndpoint)
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
                            publicNodeCache.getItem(node.id)
                                ?.also { existingNode ->
                                    if (existingNode.isOlderVariant(node)) {
                                        publicNodeCache.setItem(
                                            node.id,
                                            Constants.publicNoteExpTimeInSeconds,
                                            node
                                        )
                                    }
                                } ?: publicNodeCache.setItem(
                                node.id,
                                Constants.publicNoteExpTimeInSeconds,
                                node
                            )

                        }

                    } catch (ex: Exception) {
                        LOG.error(ex.message.toString())
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


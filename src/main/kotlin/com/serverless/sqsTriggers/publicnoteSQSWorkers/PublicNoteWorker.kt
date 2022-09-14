package com.serverless.sqsTriggers.publicnoteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.serverless.utils.Constants
import com.workduck.models.Node
import com.workduck.repositories.cache.NodeCache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager


// PublicNoteWorker Lambda is triggered via DDB Streams attached to the node entity.
class PublicNoteWorker : RequestHandler<DynamodbEvent, Void> {

    companion object {
        private val publicNodeCache = NodeCache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: Constants.DEFAULT_PUBLIC_NOTE_CACHE_ENDPOINT)
        private val LOG = LogManager.getLogger(PublicNoteWorker::class.java)
    }

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        dynamodbEvent?.also { event ->
            event.records?.let { records ->
                records.parallelStream().map { record ->
                    val newImage = record.dynamodb.newImage
                    val node: Node = newImage.toNode()

                    try {
                        //checked for value existing in cache
                        publicNodeCache.getNode(node.id)
                            ?.also { existingNode ->
                                if (existingNode.isOlderVariant(node)) {
                                    publicNodeCache.setNode(
                                        node.id,
                                        Constants.PUBLIC_NOTE_EXP_TIME_IN_SECONDS,
                                        node
                                    )
                                }
                            } ?: publicNodeCache.setNode(
                            node.id,
                            Constants.PUBLIC_NOTE_EXP_TIME_IN_SECONDS,
                            node
                        )
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
    require(this["SK"] != null) {
        "Invalid Record. NodeID not available"
    }
}.let {
    res -> Helper.objectMapper.convertValue(res, Node::class.java)
}


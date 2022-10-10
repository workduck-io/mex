package com.serverless.sqsTriggers.publicnoteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.utils.Constants
import com.workduck.models.Node
import com.workduck.repositories.cache.NodeCache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class PublicNoteSQSWorker: RequestHandler<SQSEvent, Void> {
    companion object {
        private val publicNodeCache = NodeCache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: Constants.DEFAULT_PUBLIC_NOTE_CACHE_ENDPOINT)
        private val LOG = LogManager.getLogger(PublicNoteSQSWorker::class.java)
    }
    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        sqsEvent?.also { event ->
            event.records?.let { records ->
                records.map { record ->
                    val sqsPayload = record.body
                    val message: MutableMap<String, Any?> = Helper.objectMapper.readValue(sqsPayload)
                    val nodeJSON = Helper.objectMapper.writeValueAsString(message.get("NewImage"))
                    val node = nodeJSON.toNode()

                    try {
                        if(node.hasPublicAccess()) {
                            //checked for value existing in cache
                            publicNodeCache.getNode(node.id)
                                ?.also { existingNode ->
                                    if (existingNode.isOlderVariant(node)) {
                                        publicNodeCache.setNode(
                                            node.id,
                                            node
                                        )
                                    }
                                } ?: publicNodeCache.setNode(
                                node.id,
                                node
                            )
                        } else {
                            //Remove the node from the cache if it is made private
                            publicNodeCache.deleteNode(node.id)
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

private fun String.toNode(): Node = Helper.objectMapper.readValue(this, Node::class.java)
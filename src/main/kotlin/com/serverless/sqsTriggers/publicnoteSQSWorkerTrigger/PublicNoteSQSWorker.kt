package com.serverless.sqsTriggers.publicnoteSQSWorkerTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.serverless.utils.Constants
import com.workduck.models.Node
import com.workduck.repositories.NodeCache
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
                records.parallelStream().map { record ->
                    val nodeString = record.body
                    val node: Node = nodeString.toNode()

                    try {
                        //checked for value existing in cache
                        publicNodeCache.getItem(node.id)
                            ?.also { existingNode ->
                                if (existingNode.isOlderVariant(node)) {
                                    publicNodeCache.setItem(
                                        node.id,
                                        Constants.PUBLIC_NOTE_EXP_TIME_IN_SECONDS,
                                        node
                                    )
                                }
                            } ?: publicNodeCache.setItem(
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

private fun String.toNode(): Node = Helper.objectMapper.convertValue(this, Node::class.java)
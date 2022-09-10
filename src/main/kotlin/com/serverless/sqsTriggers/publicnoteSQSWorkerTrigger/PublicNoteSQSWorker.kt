package com.serverless.sqsTriggers.publicnoteSQSWorkerTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class PublicNoteSQSWorker: RequestHandler<SQSEvent, Void> {
    private val defaultPublicNoteCacheEndpoint: String = "mex-public-note-cache.m6edlo.ng.0001.use1.cache.amazonaws.com"
    private val cacheExpTimeInSeconds: Long = 900
    private val publicNodeCache: Cache<Node> = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: defaultPublicNoteCacheEndpoint)
    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        sqsEvent?.also { event ->
            event.records?.let { records ->
                records.parallelStream().map { record ->
                    val nodeString = record.body
                    val node: Node = nodeString.toNode()

                    try {
                        takeIf { node.hasPublicAccess() }.apply {
                            //checked for value existing in cache
                            publicNodeCache.getItem(node.id)
                                ?.also { existingNode ->
                                    if (existingNode.isOlderVariant(node)) {
                                        publicNodeCache.setItem(
                                            node.id,
                                            cacheExpTimeInSeconds,
                                            node
                                        )
                                    }
                                } ?: publicNodeCache.setItem(
                                node.id,
                                cacheExpTimeInSeconds,
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

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteSQSWorker::class.java)
    }
}

private fun String.toNode(): Node = Helper.objectMapper.convertValue(this, Node::class.java)
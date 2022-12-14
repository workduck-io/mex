package com.serverless.sqsTriggers.publicnoteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.utils.CacheHelper
import com.serverless.utils.Constants
import com.workduck.repositories.cache.NodeCache
import com.workduck.utils.Helper
import com.workduck.utils.extensions.toNode

class PublicNoteSQSWorker: RequestHandler<SQSEvent, Void> {
    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        sqsEvent?.also { event ->
            event.records?.let { records ->
                records.map { record ->
                    val publicNodeCache = NodeCache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: Constants.DEFAULT_PUBLIC_NOTE_CACHE_ENDPOINT)
                    val sqsPayload = record.body
                    val message: MutableMap<String, Any?> = Helper.objectMapper.readValue(sqsPayload)
                    val nodeJSON = Helper.objectMapper.writeValueAsString(message.get("NewImage"))
                    val node = nodeJSON.toNode()
                    val encodedKey = CacheHelper.encodePublicCacheKey(node.id)

                    try {
                        if(node.hasPublicAccess()) {
                            //checked for value existing in cache
                            publicNodeCache.getNode(encodedKey)
                                ?.also { existingNode ->
                                    if (existingNode.isOlderVariant(node)) {
                                        publicNodeCache.setNode(
                                            encodedKey,
                                            node
                                        )
                                    }
                                } ?: publicNodeCache.setNode(
                                encodedKey,
                                node
                            )
                        } else {
                            //Remove the node from the cache if it is made private
                            publicNodeCache.deleteNode(encodedKey)
                        }
                    } finally {
                        publicNodeCache.closeConnection()
                    }
                }
            }
        }
        return null
    }
}

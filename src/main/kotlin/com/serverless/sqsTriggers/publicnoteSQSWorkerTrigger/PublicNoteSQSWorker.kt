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
    override fun handleRequest(input: SQSEvent?, context: Context?): Void? {

        input?.records?.let{
            for (record in input.records) {
                val jsonResult = record.body
                val nodeObject : Node = Helper.objectMapper.convertValue(jsonResult, Node::class.java)
                val nodeID = nodeObject.id

                try {
                    if(nodeObject.publicAccess) {
                        val existingPublicNote = publicNodeCache.getItem(nodeID)
                        if(existingPublicNote != null) {
                            val existingNode : Node = existingPublicNote
                            if(existingNode.updatedAt < nodeObject.updatedAt)
                                publicNodeCache.setItem(nodeID, cacheExpTimeInSeconds, nodeObject)
                        } else {
                            publicNodeCache.setItem(nodeID, cacheExpTimeInSeconds, nodeObject)
                        }
                    }
                    publicNodeCache.closeConnection()
                } catch (ex: Exception) {
                    LOG.error(ex.message.toString())
                }
            }
        }
        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteSQSWorker::class.java)
    }
}
package com.serverless.sqsTriggers.publicnoteSQSWorkerTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class PublicNoteSQSWorker: RequestHandler<SQSEvent, Void> {
    private val defaultPublicNoteCacheEndpoint: String = "mex-public-note-cache.m6edlo.ng.0001.use1.cache.amazonaws.com"
    private val cacheExpTimeInSeconds: Long = 900
    private val publicNodeCache: Cache = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: defaultPublicNoteCacheEndpoint)
    override fun handleRequest(input: SQSEvent?, context: Context?): Void? {

        input?.records?.let{
            for (record in input.records) {
                val jsonResult = record.body as MutableMap<String?, Any?>
                val nodeObject : Node = Helper.objectMapper.convertValue(jsonResult, Node::class.java)
                val nodeID = nodeObject.id

                try {
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
                }
            }
        }
        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteSQSWorker::class.java)
    }
}
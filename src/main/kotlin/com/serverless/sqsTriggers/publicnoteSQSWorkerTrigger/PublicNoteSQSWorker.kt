package com.serverless.sqsTriggers.publicnoteSQSWorkerTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.Message
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class PublicNoteSQSWorker: RequestHandler<SQSEvent, Void> {
    private val defaultPublicNoteCacheEndpoint: String = "mex-public-note-cache.m6edlo.ng.0001.use1.cache.amazonaws.com"
    private val cacheExpTimeInSeconds: Long = 900

    override fun handleRequest(input: SQSEvent?, context: Context?): Void? {
        val publicNodeCache: Cache = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: defaultPublicNoteCacheEndpoint)

        LOG.debug("insidee ${input.toString()}")

        input?.records?.let{
            for (record in input.records) {
                val nodeObject : Node = Helper.objectMapper.convertValue(record, Node::class.java)
                val nodeID = nodeObject.id
                val existingPublicNote = publicNodeCache.get(nodeID.toString())
                LOG.debug(nodeObject.toString())
                try {
                    if(existingPublicNote != null) {
                        val existingNodeObject : Node = Helper.objectMapper.convertValue(existingPublicNote, Node::class.java)
                        if(existingNodeObject.updatedAt < nodeObject.updatedAt)
                            publicNodeCache.set(nodeID.toString(), cacheExpTimeInSeconds, nodeObject.toString())
                    } else publicNodeCache.set(nodeID.toString(), cacheExpTimeInSeconds, nodeObject.toString())
                } catch (ex: Exception) {
                    LOG.error(ex.message.toString())
                }
            }
        }
        publicNodeCache.closeConnection()
        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteSQSWorker::class.java)
    }
}
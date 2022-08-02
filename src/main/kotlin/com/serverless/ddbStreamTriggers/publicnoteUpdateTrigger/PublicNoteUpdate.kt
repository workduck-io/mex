package com.serverless.ddbStreamTriggers.publicnoteUpdateTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class PublicNoteUpdate : RequestHandler<DynamodbEvent, Void> {
    private val defaultPublicNoteCacheEndpoint: String = "mex-public-note-cache.m6edlo.ng.0001.use1.cache.amazonaws.com"
    private val publicNodeCache: Cache = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: defaultPublicNoteCacheEndpoint)
    private val cacheExpTimeInSeconds: Long = 900

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        try {
            dynamodbEvent?.records?.let{
                for (record in dynamodbEvent.records) {
                    val newImage = record.dynamodb.newImage
                    val nodeID = newImage["SK"]?.s ?: throw Exception("Invalid Record. NodeID not available")
                    val publicAccess = newImage["publicAccess"]?.s ?: throw Exception("Invalid Record. PublicAccess not available")

                    // Check for public access update for the node
                    if(publicAccess == "true") {
                        val jsonResult = Helper.mapToJson(newImage).toMutableMap()
                        val nodeObject : Node = Helper.objectMapper.convertValue(jsonResult.toString(), Node::class.java)
                        publicNodeCache.set(nodeID.toString(), cacheExpTimeInSeconds, nodeObject.toString())
                    }
                }
            }
        } catch (exception: Exception) {
            LOG.error(exception.message.toString())
        }
        publicNodeCache.closeConnection()
        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteUpdate::class.java)
    }
}
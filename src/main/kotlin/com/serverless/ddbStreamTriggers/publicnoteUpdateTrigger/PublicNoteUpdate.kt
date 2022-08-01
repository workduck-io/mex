package com.serverless.ddbStreamTriggers.publicnoteUpdateTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class PublicNoteUpdate : RequestHandler<DynamodbEvent, Void> {
    private val publicNodeCache: Cache = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT"))
    private val cacheExpTimeInSeconds: Long = 900

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        try {
            if(dynamodbEvent == null || dynamodbEvent.records == null) return null /* will be the case when warmup lambda calls it */

            for (record in dynamodbEvent.records) {
                val newImage = record.dynamodb.newImage
                val nodeID = newImage["SK"]?.s ?: throw Exception("Invalid Record. NodeID not available")
                val jsonResult = Helper.mapToJson(newImage).toMutableMap()
                val nodeObject : Node = Helper.objectMapper.convertValue(jsonResult.toString(), Node::class.java)

                // Check for public access update for the node
                if(nodeObject.publicAccess) {
                    publicNodeCache.setEx(nodeID.toString(), cacheExpTimeInSeconds, nodeObject.toString())
                }
            }
        } catch (exception: Exception) {
            LOG.error(exception.message.toString())
        }

        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteUpdate::class.java)
    }
}
package com.serverless.ddbStreamTriggers.publicnoteUpdateTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.workduck.repositories.Cache
import org.apache.logging.log4j.LogManager

class PublicNoteUpdate : RequestHandler<DynamodbEvent, Void> {
    private val publicNodeCache: Cache = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT"))
    private val cacheExpTimeInSeconds: Long = 900

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        try {
            if(dynamodbEvent == null || dynamodbEvent.records == null) return null /* will be the case when warmup lambda calls it */

            for (record in dynamodbEvent.records) {
                val nodeID = record.dynamodb.newImage["SK"]?.s ?: throw Exception("Invalid Record. NodeID not available")
                val newPublicAccessValue = record.dynamodb.newImage["publicAccess"]?.n?.toInt()
                val oldPublicAccessValue = record.dynamodb.oldImage["publicAccess"]?.n?.toInt()

                LOG.debug("New public access: $newPublicAccessValue")
                LOG.debug("Old public access: $oldPublicAccessValue")

                // Check for public access update for the node
                if(oldPublicAccessValue == 0 && newPublicAccessValue == 1) {
                    LOG.debug("inside the if before cache push")
                    //            val nodeDetail: Node = gson.fromJson(ItemUtils.toItem(record.dynamodb.newImage).toJSON(), Node::class.java)
//                    val node : Node = objectMapper.readValue(objectMapper.writeValueAsString(ddbPayload.NewImage))

                    publicNodeCache.setEx(nodeID.toString(), cacheExpTimeInSeconds, record.dynamodb.newImage.toString())

                    val cachedValue = publicNodeCache.get(nodeID.toString())

                    LOG.debug("value: $cachedValue")

                    LOG.debug("inside the if after cache push")
                }
            }
        } catch (exception: Exception) {
            LOG.debug(exception.message.toString())
        }

        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteUpdate::class.java)
    }

}
package com.serverless.sqsTriggers.taskEntitySQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.utils.Constants
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class TaskEntityWorker : RequestHandler<SQSEvent, Void> {
    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        sqsEvent?.also { event ->
            event.records?.let { records ->
                records.map { record ->
                    val sqsPayload = record.body
                    val taskEntityBody: MutableMap<String, Any?> = Helper.objectMapper.readValue(sqsPayload)
                    val eventType = taskEntityBody.get("EventName")

                    @Suppress("UNCHECKED_CAST")
                    val newImage = taskEntityBody.get("NewImage") as MutableMap<String, Any?>
                    try {
                        when (eventType) {
                            Constants.DDB_INSERT -> {
                                TaskEntityHelper.handleInsertEvent(newImage)
                            }
                            Constants.DDB_MODIFY -> {
                                if(newImage.get("_status") == Constants.ENTITY_ARCHIVED_STATUS)
                                    // Delete the archived task from the node
                                    TaskEntityHelper.handleDeleteTaskBlock(newImage)
                            }
                            Constants.DDB_REMOVE -> {
                                TODO("Yet to be implemented")
                            }
                        }
                    } catch (ex: Exception) {
                        LOG.error(ex.message.toString())
                    }
                }
            }
        }
        return null
    }
    
    companion object {
        private val LOG = LogManager.getLogger(TaskEntityWorker::class.java)
    }
}
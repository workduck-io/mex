package com.serverless.sqsTriggers.taskEntitySQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.serverless.utils.Constants
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class TaskEntityDLQWorker: RequestHandler<DynamodbEvent, Void> {
    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context?): Void? {
        dynamodbEvent?.records?.let {
            for (record in dynamodbEvent.records) {
                val newImage = record.dynamodb.newImage
                newImage["sk"]?.s ?: throw Exception("Invalid Record. EntityID not available")
                newImage["pk"]?.s ?: throw Exception("Invalid Record. WorkspaceID not available")
                val taskEntityJSON = Helper.mapToJson(newImage).toMutableMap()

                when (record.eventName) {
                    Constants.DDB_STREAM_INSERT -> {
                        TaskEntityHelper.handleInsertEvent(taskEntityJSON)
                    }
                    Constants.DDB_STREAM_MODIFY -> {
                        TODO("Yet to be implemented")
                    }
                    Constants.DDB_STREAM_REMOVE -> {
                        TaskEntityHelper.handleRemoveEvent(taskEntityJSON)
                    }
                }
            }
        }
        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(TaskEntityDLQWorker::class.java)
    }
}
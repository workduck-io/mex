package com.serverless.sqsTriggers.nodeDeleteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent

class NodeDeleteDLQWorker: RequestHandler<SQSEvent, Void> {
    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        return NodeDeleteWorker().handleRequest(sqsEvent, context)
    }

}

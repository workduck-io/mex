package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent

class NamespaceDeleteDLQWorker: RequestHandler<SQSEvent, Void> {
    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        return NamespaceDeleteWorker().handleRequest(sqsEvent, context)
    }

}

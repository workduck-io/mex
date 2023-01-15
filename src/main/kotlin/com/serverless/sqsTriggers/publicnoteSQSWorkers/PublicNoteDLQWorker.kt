package com.serverless.sqsTriggers.publicnoteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent

class PublicNoteSQSWorker: RequestHandler<SQSEvent, Void> {
    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        return PublicNoteWorker().handleRequest(sqsEvent, context)
    }

}

package com.serverless.sqsTriggers.nodeDeleteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.serverless.sqsTriggers.utils.Helper
import com.serverless.sqsTriggers.utils.SNSHelper

class NodeDeleteDLQWorker: RequestHandler<SQSEvent, Void> {

    companion object {
        val snsClient = Helper.createSNSConnection()
    }

    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        return try {
            NodeDeleteWorker().handleRequest(sqsEvent, context)
        } catch (e : Exception){
            SNSHelper.publishExceptionToSNSTopic(e, snsClient)
            null
        }
    }

}

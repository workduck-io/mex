package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.google.gson.Gson
import com.serverless.sqsNodeEventHandlers.DDBPayload
import com.serverless.sqsTriggers.nodeDeleteSQSWorkers.NodeDeleteWorker
import com.serverless.sqsTriggers.utils.Constants
import com.serverless.sqsTriggers.utils.Helper
import com.serverless.sqsTriggers.utils.PayloadProcessor
import com.workduck.models.Namespace
import com.workduck.service.NodeService
import com.workduck.utils.extensions.toNamespace
import com.workduck.utils.extensions.toSQSPayload
import org.apache.logging.log4j.LogManager

class NamespaceDeleteWorker: RequestHandler<SQSEvent, Void> {

    companion object {
        private val LOG = LogManager.getLogger(NamespaceDeleteWorker::class.java)
        val nodeService = NodeService()
        val sqs = Helper.createSQSConnection()
    }

    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        sqsEvent?.also { event ->
            event.records?.let { records ->
                records.map { record ->
                    val ddbPayload : DDBPayload = PayloadProcessor.process(record.body.toSQSPayload())
                    val namespace : Namespace = ddbPayload.NewImage!!.toNamespace()
                    LOG.info("${namespace.id} has been softDeleted") // TODO ( remove when the feature is stable )
                    NamespaceDeleteStrategyFactory.getNamespaceDeleteStrategy(namespace).deleteNamespace(namespace, nodeService)

                    sqs.deleteMessage(Constants.namespaceDeleteSQSURL, record.receiptHandle)
                }
            }
        }
        return null
    }

}

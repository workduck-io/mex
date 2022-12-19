package com.serverless.sqsTriggers.nodeDeleteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.serverless.sqsNodeEventHandlers.DDBPayload
import com.serverless.sqsTriggers.namespaceDeleteSQSWorkers.NamespaceDeleteWorker
import com.serverless.sqsTriggers.utils.PayloadProcessor
import com.workduck.models.Node
import com.workduck.service.NodeService
import com.workduck.utils.Helper
import com.workduck.utils.extensions.toNode
import com.workduck.utils.extensions.toSQSPayload
import org.apache.logging.log4j.LogManager

class NodeDeleteWorker : RequestHandler<SQSEvent, Void> {

    companion object {
        private val LOG = LogManager.getLogger(NodeDeleteWorker::class.java)
        val nodeService = NodeService()
    }

    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        sqsEvent?.also { event ->
            event.records?.let { records ->
                records.map { record ->
                    val ddbPayload : DDBPayload = PayloadProcessor.process(record.body.toSQSPayload())
                    val node : Node = ddbPayload.NewImage!!.toNode()
                    NodeDeleteStrategyFactory.getNodeDeleteStrategy(node).deleteNode(node, NamespaceDeleteWorker.nodeService)
                }
            }
        }
        return null
    }
}



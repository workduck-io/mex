package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService
import com.workduck.service.NodeService
import com.workduck.utils.Helper
import com.workduck.utils.extensions.toMap
import com.workduck.utils.extensions.toNamespace
import org.apache.logging.log4j.LogManager

class NamespaceDeleteWorker: RequestHandler<SQSEvent, Void> {

    companion object {
        private val LOG = LogManager.getLogger(NamespaceDeleteWorker::class.java)

        val nodeService = NodeService()
    }

    override fun handleRequest(sqsEvent: SQSEvent?, context: Context?): Void? {
        sqsEvent?.also { event ->
            event.records?.let { records ->
                records.map { record ->
                    LOG.info(Gson().toJson(record))
                    val body = record.body.toMap()
                    val namespace : Namespace = body["NewImage"]!!.toNamespace()
                    LOG.info("${namespace.id} has been softDeleted")
                    NamespaceDeleteStrategyFactory.getNamespaceDeleteStrategy(namespace).deleteNamespace(namespace, nodeService)
                }
            }
        }
        return null
    }

}

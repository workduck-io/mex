package com.serverless.sqsTriggers.nodeDeleteSQSWorkers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.serverless.sqsTriggers.namespaceDeleteSQSWorkers.NamespaceDeleteStrategyFactory
import com.serverless.sqsTriggers.namespaceDeleteSQSWorkers.NamespaceDeleteWorker
import com.workduck.models.Node
import com.workduck.service.NodeService
import com.workduck.utils.Helper
import com.workduck.utils.TagHelper
import com.workduck.utils.extensions.toMap
import com.workduck.utils.extensions.toNode
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
                    LOG.info(Gson().toJson(record))
                    val body = record.body.toMap()
                    val node : Node = body["NewImage"]!!.toNode()
                    NodeDeleteStrategyFactory.getNodeDeleteStrategy(node).deleteNode(node, NamespaceDeleteWorker.nodeService)
                }
            }
        }
        return null
    }
}


fun main(){
    val body = "{\"streamARN\":\"arn:aws:dynamodb:us-east-1:418506370286:table/local-mex/stream/2022-12-05T11:58:40.717\",\"NewImage\":{\"itemType\":\"Node\",\"nodeData\":{\"TEMP_ztJW3\":\"{\\\"id\\\":\\\"TEMP_ztJW3\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_6Wp6T\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924351234,\\\"updatedAt\\\":1670924351234}\",\"TEMP_YcnFX\":\"{\\\"id\\\":\\\"TEMP_YcnFX\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_AmCiM\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"},{\\\"id\\\":\\\"TEMP_eGia9\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_WAmez\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"tag\\\",\\\"properties\\\":{\\\"value\\\":\\\"commonTag\\\"}},{\\\"id\\\":\\\"TEMP_gXxjB\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924351234,\\\"updatedAt\\\":1670924351234}\",\"TEMP_ejw8R\":\"{\\\"id\\\":\\\"TEMP_ejw8R\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_VTXb6\\\",\\\"content\\\":\\\"Hey! My name is Varun. I'll now be testing tag creation for mex-backend.\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670923991809,\\\"updatedAt\\\":1670924351257}\",\"TEMP_K8aUa\":\"{\\\"id\\\":\\\"TEMP_K8aUa\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_Q9mhT\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"},{\\\"id\\\":\\\"TEMP_qFLBr\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_h4F6V\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"tag\\\",\\\"properties\\\":{\\\"value\\\":\\\"test1\\\"}},{\\\"id\\\":\\\"TEMP_J7mED\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924351234,\\\"updatedAt\\\":1670924351234}\",\"TEMP_GHCfc\":\"{\\\"id\\\":\\\"TEMP_GHCfc\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_8exzU\\\",\\\"content\\\":\\\"Hello\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924505350,\\\"updatedAt\\\":1670924505350}\",\"TEMP_PTdpy\":\"{\\\"id\\\":\\\"TEMP_PTdpy\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_Niq4p\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924351234,\\\"updatedAt\\\":1670924351234}\"},\"publicAccess\":0,\"AK\":\"NAMESPACE_aRmtqjcrhKQ9WXXzqbUEJ\",\"expireAt\":1676116125,\"title\":\"Testing Tags\",\"version\":3,\"lastEditedBy\":\"45135611-f861-4de2-9e1f-782e4c69ec3b\",\"tags\":[\"test1\",\"commonTag\"],\"dataOrder\":[\"TEMP_ejw8R\",\"TEMP_PTdpy\",\"TEMP_K8aUa\",\"TEMP_YcnFX\",\"TEMP_ztJW3\",\"TEMP_GHCfc\"],\"createdAt\":1670923991809,\"deleted\":1,\"createdBy\":\"45135611-f861-4de2-9e1f-782e4c69ec3b\",\"itemStatus\":\"ACTIVE\",\"SK\":\"NODE_MYa6ctwix9haCUqndzjW4\",\"PK\":\"WORKSPACE_rfTyaEPeTKB6B3jaq6Vxj\",\"nodeVersionCount\":0,\"updatedAt\":1670932125687},\"OldImage\":{\"itemType\":\"Node\",\"nodeData\":{\"TEMP_ztJW3\":\"{\\\"id\\\":\\\"TEMP_ztJW3\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_6Wp6T\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924351234,\\\"updatedAt\\\":1670924351234}\",\"TEMP_YcnFX\":\"{\\\"id\\\":\\\"TEMP_YcnFX\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_AmCiM\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"},{\\\"id\\\":\\\"TEMP_eGia9\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_WAmez\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"tag\\\",\\\"properties\\\":{\\\"value\\\":\\\"commonTag\\\"}},{\\\"id\\\":\\\"TEMP_gXxjB\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924351234,\\\"updatedAt\\\":1670924351234}\",\"TEMP_ejw8R\":\"{\\\"id\\\":\\\"TEMP_ejw8R\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_VTXb6\\\",\\\"content\\\":\\\"Hey! My name is Varun. I'll now be testing tag creation for mex-backend.\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670923991809,\\\"updatedAt\\\":1670924351257}\",\"TEMP_K8aUa\":\"{\\\"id\\\":\\\"TEMP_K8aUa\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_Q9mhT\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"},{\\\"id\\\":\\\"TEMP_qFLBr\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_h4F6V\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"tag\\\",\\\"properties\\\":{\\\"value\\\":\\\"test1\\\"}},{\\\"id\\\":\\\"TEMP_J7mED\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924351234,\\\"updatedAt\\\":1670924351234}\",\"TEMP_GHCfc\":\"{\\\"id\\\":\\\"TEMP_GHCfc\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_8exzU\\\",\\\"content\\\":\\\"Hello\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924505350,\\\"updatedAt\\\":1670924505350}\",\"TEMP_PTdpy\":\"{\\\"id\\\":\\\"TEMP_PTdpy\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_Niq4p\\\",\\\"content\\\":\\\"\\\",\\\"elementType\\\":\\\"p\\\"}],\\\"elementType\\\":\\\"p\\\",\\\"createdBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"lastEditedBy\\\":\\\"45135611-f861-4de2-9e1f-782e4c69ec3b\\\",\\\"createdAt\\\":1670924351234,\\\"updatedAt\\\":1670924351234}\"},\"publicAccess\":0,\"AK\":\"NAMESPACE_aRmtqjcrhKQ9WXXzqbUEJ\",\"expireAt\":1676115864,\"title\":\"Testing Tags\",\"version\":3,\"lastEditedBy\":\"45135611-f861-4de2-9e1f-782e4c69ec3b\",\"tags\":[\"test1\",\"commonTag\"],\"dataOrder\":[\"TEMP_ejw8R\",\"TEMP_PTdpy\",\"TEMP_K8aUa\",\"TEMP_YcnFX\",\"TEMP_ztJW3\",\"TEMP_GHCfc\"],\"createdAt\":1670923991809,\"createdBy\":\"45135611-f861-4de2-9e1f-782e4c69ec3b\",\"itemStatus\":\"ACTIVE\",\"SK\":\"NODE_MYa6ctwix9haCUqndzjW4\",\"PK\":\"WORKSPACE_rfTyaEPeTKB6B3jaq6Vxj\",\"nodeVersionCount\":0,\"updatedAt\":1670931864080},\"EventName\":\"MODIFY\",\"Type\":\"DDBPayload\",\"queueUrl\":\"https://sqs.us-east-1.amazonaws.com/418506370286/node-delete-local.fifo\"}"

    val message: MutableMap<String, Any?> = Helper.objectMapper.readValue(body)
    println(message)
    val newImage = message["NewImage"]!!
    println(newImage)
    val node : Node = Helper.objectMapper.convertValue(newImage)
    println(node)
}


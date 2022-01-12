package com.serverless.sqsNodeEventHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.service.NodeService
import com.workduck.utils.Helper


const val bdy = "{\"NewImage\":{\"itemType\":\"Node\",\"nodeData\":{\"sampleParentID\":\"{\\\"id\\\":\\\"sampleParentID\\\",\\\"parentID\\\":null,\\\"content\\\":null,\\\"children\\\":[{\\\"id\\\":\\\"sampleChildID\\\",\\\"parentID\\\":null,\\\"content\\\":\\\"sample child content 1\\\",\\\"children\\\":null,\\\"elementType\\\":\\\"paragraph\\\",\\\"properties\\\":{\\\"bold\\\":true,\\\"italic\\\":true},\\\"createdBy\\\":null,\\\"lastEditedBy\\\":null,\\\"createdAt\\\":null,\\\"updatedAt\\\":null}],\\\"elementType\\\":\\\"paragraph\\\",\\\"properties\\\":null,\\\"createdBy\\\":\\\"Varun\\\",\\\"lastEditedBy\\\":\\\"Varun\\\",\\\"createdAt\\\":1639752464974,\\\"updatedAt\\\":1639752464974}\"},\"publicAccess\":0,\"namespaceIdentifier\":\"NAMESPACE1\",\"workspaceIdentifier\":\"WORKSPACE1\",\"AK\":\"WORKSPACE1#NAMESPACE1\",\"dataOrder\":[\"sampleParentID\"],\"version\":1,\"lastEditedBy\":\"Varun\",\"createdAt\":1639752464974,\"createdBy\":\"Varun\",\"itemStatus\":\"ACTIVE\",\"SK\":\"NODE1\",\"PK\":\"NODE1\",\"nodeVersionCount\":0,\"updatedAt\":1639752464974},\"EventName\":\"INSERT\", \"Type\":\"DDBPayload\"}"

/* Lambda function which puts message in the SQS, is already converting DDB JSON to POJO JSON */

class SQSEventHandler : RequestHandler<SQSEvent, Any> {

    val nodeService = NodeService()
    override fun handleRequest(input: SQSEvent, context: Context) {
        val objectMapper = Helper.objectMapper

        println("SQS Event : $input")
        for (msg in input.records) {

            val ddbPayload = PayloadProcessor.process(objectMapper.readValue(msg.body))

            println("ddbPayload : $ddbPayload")

            try {
                EventHelper.processDDBPayload(ddbPayload, nodeService)
            } catch (e : Error){
                throw Exception("Error processing payload : $e")
            }

            try{
                val queueUrl = "https://sqs.us-east-1.amazonaws.com/418506370286/DDBStreamLambdaQueueTest"
                EventHelper.deleteMessageFromSQS(queueUrl, msg.receiptHandle)
            } catch (e : Error){
                throw Exception("Error deleting message from SQS : $e")
            }

        }
    }
}


fun main(){
    val ddbPayload = PayloadProcessor.process(Helper.objectMapper.readValue(bdy))
    println("Message Body Object : $ddbPayload")
    val new = ddbPayload.NewImage
    println("New : $new")
    EventHelper.processDDBPayload(ddbPayload, NodeService())

}

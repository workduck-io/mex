package com.serverless.sqsNodeEventHandlers

import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.workduck.utils.Helper
import com.workduck.service.NodeService

object EventHelper {

   val objectMapper = Helper.objectMapper

   fun processDDBPayload(ddbPayload: DDBPayload, nodeService: NodeService) {
      val action = ActionFactory.getAction(ddbPayload.EventName)

      if (action == null) {
         println("Some error!!")
         return
      }
      println("DDB Payload in Processor : $ddbPayload")
      action.apply(ddbPayload, nodeService)
   }

   fun deleteMessageFromSQS(queueUrl : String, msgReceiptHandle : String){
      val sqs: AmazonSQS = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1).build()
      sqs.deleteMessage(queueUrl, msgReceiptHandle)

   }
}
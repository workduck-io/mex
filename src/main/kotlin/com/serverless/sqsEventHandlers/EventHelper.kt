package com.serverless.sqsEventHandlers

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.workduck.utils.Helper
import com.fasterxml.jackson.module.kotlin.readValue

object EventHelper {

   val objectMapper = Helper.objectMapper


   fun getImageObjectFromImage(imageString : String) : Image {
      return objectMapper.readValue(imageString)
   }

   fun processDDBPayload(ddbPayload: DDBPayload) {
      val action = ActionFactory.getAction(ddbPayload.EventName)

      if (action == null) {
         println("Some error!!")
         return
      }
      println("DDB Payload in Processor : $ddbPayload")
      action.apply(ddbPayload)
   }

   fun deleteMessageFromSQS(queueUrl : String, msgReceiptHandle : String){
      val sqs: AmazonSQS = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1).build()
      sqs.deleteMessage(queueUrl, msgReceiptHandle)

   }
}
package com.serverless.s3EventHandlers

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.util.IOUtils
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.eventUtils.ActionFactory
import com.workduck.utils.Helper


class S3EventHandler : RequestHandler<S3Event, Any>  {

    val objectMapper = Helper.objectMapper
    override fun handleRequest(event: S3Event, context: Context) {
        val s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1)
                .build()

        for(record in event.records){
            val key = record.s3.`object`.key
            val bucket = record.s3.bucket.name

            println("KEY : $key & BUCKET = $bucket")
            val objData = s3Client.getObject(GetObjectRequest(bucket, key)).objectContent

            val objString : String = IOUtils.toString(objData)
            println("objString : $objString")

            val actualObj: Map<String, Any> = objectMapper.readValue(objString)

            println("Hopefully working fine")

            val action = ActionFactory.getAction(actualObj["EventName"] as String)

            if (action == null) {
                println("Some error!!")
                return
            }

            action.apply(actualObj)


        }


    }

}
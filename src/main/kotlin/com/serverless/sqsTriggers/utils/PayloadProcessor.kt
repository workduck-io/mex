package com.serverless.sqsTriggers.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.util.IOUtils
import com.serverless.sqsNodeEventHandlers.DDBPayload
import com.serverless.sqsNodeEventHandlers.S3Payload
import com.serverless.sqsNodeEventHandlers.SQSPayload
import com.workduck.utils.extensions.toDDBPayload


object PayloadProcessor {

    //TODO(use generics)
    fun process(sqsPayload: SQSPayload) : DDBPayload = when(sqsPayload){
        is DDBPayload -> process(sqsPayload)
        is S3Payload -> process(sqsPayload)
    }

    private fun process(ddbPayload: DDBPayload) : DDBPayload = ddbPayload

    private fun process(s3Payload: S3Payload) : DDBPayload {
        val s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1)
            .build()
        println("Getting object from S3. Key = ${s3Payload.Key}")
        val objString : String = IOUtils.toString(s3Client.getObject("mex", s3Payload.Key).objectContent)
        return objString.toDDBPayload()
    }
}
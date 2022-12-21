package com.serverless.sqsTriggers.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder

object Helper {
    fun createSQSConnection() : AmazonSQS = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1).build()



}
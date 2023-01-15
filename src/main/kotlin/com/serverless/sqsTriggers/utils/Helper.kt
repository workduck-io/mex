package com.serverless.sqsTriggers.utils

import com.amazonaws.regions.Regions

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient


object Helper {
    fun createSQSConnection() : AmazonSQS = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1).build()

    fun createSNSConnection() : SnsClient = SnsClient.builder().region(Region.US_EAST_1).build()

}
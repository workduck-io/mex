package com.serverless.sqsTriggers.utils

import com.workduck.utils.DDBHelper
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import java.lang.Exception

object SNSHelper {

    fun getSNSTopicArn() : String = when(DDBHelper.getStage()){
        "prod" -> "arn:aws:sns:us-east-1:455613468381:${DDBHelper.getStage()}-mex-backend-dlq-sns"
         else -> "arn:aws:sns:us-east-1:418506370286:dlq-sns-${DDBHelper.getStage()}"
    }


    fun publishExceptionToSNSTopic(exception: Exception, snsClient: SnsClient) {
        val request = PublishRequest.builder()
            .message(getPrettyMessageFormat(exception))
            .topicArn(getSNSTopicArn())
            .build()
        snsClient.publish(request)
    }

    fun getPrettyMessageFormat(exception: Exception) : String{

        var message = "Error  summary" + "\n"
        message += "##########################################################\n"
        message += "# Message :- " + exception.message + "\n"
        message += "# Stack Trace:- " + "\n"
        message += "#" + exception.stackTraceToString().split("\n")
        message += "\n##########################################################"
        return message
    }
}
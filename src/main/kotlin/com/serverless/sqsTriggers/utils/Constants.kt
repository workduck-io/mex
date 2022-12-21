package com.serverless.sqsTriggers.utils

import com.workduck.utils.DDBHelper

object Constants {

    val namespaceDeleteSQSURL  = "https://sqs.us-east-1.amazonaws.com/418506370286/namespace-delete-${DDBHelper.getStage()}.fifo"
    val nodeDeleteSQSURL  = "https://sqs.us-east-1.amazonaws.com/418506370286/node-delete-${DDBHelper.getStage()}.fifo"
}
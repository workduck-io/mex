package com.serverless.sqsEventHandlers

interface Action {
    fun apply(ddbPayload: DDBPayload)
}
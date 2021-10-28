package com.serverless

class Response(message: String, input: Map<String, Any>) : Responses {
    val message: String = message
    val input: Map<String, Any> = input
}

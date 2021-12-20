package com.serverless.eventUtils

interface Action {
    fun apply(message : Map<String, Any>)
}
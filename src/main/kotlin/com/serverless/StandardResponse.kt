package com.serverless

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class StandardResponse(message:String) : Responses {
	val message: String = message
}
package com.serverless

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class StandardResponse(passedObject :Any) : Responses {
	val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
	val message: String = objectMapper.writeValueAsString(passedObject)
}
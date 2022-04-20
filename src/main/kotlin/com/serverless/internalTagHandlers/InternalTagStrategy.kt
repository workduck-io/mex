package com.serverless.internalTagHandlers

import com.serverless.ApiGatewayResponse
import com.workduck.service.TagService

interface InternalTagStrategy {
    fun apply(input: TagInput, tagService: TagService): ApiGatewayResponse
}
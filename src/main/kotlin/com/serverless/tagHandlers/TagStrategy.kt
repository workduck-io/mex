package com.serverless.tagHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.TagService

interface TagStrategy {
    fun apply(input: Input, tagService: TagService): ApiGatewayResponse
}
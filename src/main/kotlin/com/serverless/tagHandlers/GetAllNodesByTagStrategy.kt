package com.serverless.tagHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.TagService

class GetAllNodesByTagStrategy : TagStrategy {
    override fun apply(input: Input, tagService: TagService): ApiGatewayResponse {
        TODO("Not yet implemented")
    }

}

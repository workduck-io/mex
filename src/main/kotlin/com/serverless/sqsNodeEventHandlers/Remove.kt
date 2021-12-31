package com.serverless.sqsNodeEventHandlers

import com.workduck.service.NodeService
import com.workduck.utils.Helper

class Remove : Action {
    override fun apply(ddbPayload: DDBPayload, nodeService: NodeService) {
        val oldNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(ddbPayload.OldImage)) as NodeImage

        println(Helper.objectMapper.writeValueAsString(oldNode))
    }
}
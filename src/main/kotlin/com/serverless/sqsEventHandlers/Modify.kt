package com.serverless.sqsEventHandlers

import com.workduck.service.NodeService
import com.workduck.utils.Helper

class Modify : Action {
    override fun apply(ddbPayload: DDBPayload) {

        val oldNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(ddbPayload.OldImage)) as NodeImage
        val newNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(ddbPayload.NewImage)) as NodeImage

        newNode.id?.let { NodeService().checkNodeVersionCount(it, newNode.nodeVersionCount) }
    }
}
package com.serverless.eventUtils

import com.serverless.sqsEventHandlers.EventHelper
import com.workduck.service.NodeService
import com.workduck.utils.Helper

class Modify : Action {
    override fun apply(message : Map<String, Any>) {

        val oldNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(message["OldImage"])) as NodeImage
        val newNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(message["NewImage"])) as NodeImage

        newNode.id?.let { NodeService().checkNodeVersionCount(it, newNode.nodeVersionCount) }
    }
}
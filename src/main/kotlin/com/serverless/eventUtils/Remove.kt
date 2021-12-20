package com.serverless.eventUtils

import com.serverless.sqsEventHandlers.EventHelper
import com.workduck.utils.Helper

class Remove : Action {
    override fun apply(message : Map<String, Any>) {
        val oldNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(message["OldImage"])) as NodeImage

        println(Helper.objectMapper.writeValueAsString(oldNode))
    }
}
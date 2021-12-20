package com.serverless.eventUtils

import com.serverless.sqsEventHandlers.EventHelper
import com.workduck.utils.Helper


class Insert : Action {
    override fun apply(message: Map<String, Any>) {

        val newNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(message["NewImage"])) as NodeImage

        println(Helper.objectMapper.writeValueAsString(newNode))
    }
}
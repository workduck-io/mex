package com.serverless.sqsEventHandlers

import com.workduck.utils.Helper

class Remove : Action {
    override fun apply(ddbPayload: DDBPayload) {
        val oldNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(ddbPayload.OldImage)) as NodeImage

        println(Helper.objectMapper.writeValueAsString(oldNode))
    }
}
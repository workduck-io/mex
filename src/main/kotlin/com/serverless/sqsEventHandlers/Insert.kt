package com.serverless.sqsEventHandlers

import com.workduck.utils.Helper


class Insert : Action {
    override fun apply(ddbPayload: DDBPayload) {


        val newNode = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(ddbPayload.NewImage))

        println(Helper.objectMapper.writeValueAsString(newNode))
    }
}
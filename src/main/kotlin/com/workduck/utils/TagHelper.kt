package com.workduck.utils

import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Tag

object TagHelper {

    fun convertObjectToTag(tagObject : Any) : Tag {
        return Helper.objectMapper.readValue(Helper.objectMapper.writeValueAsString(tagObject))
    }

    fun getNodesMapFromOutcomeObject(newValues: UpdateItemOutcome) : HashMap<String, String> {
        return Helper.objectMapper.readValue(Helper.objectMapper.writeValueAsString(newValues.item["nodes"]))
    }
}
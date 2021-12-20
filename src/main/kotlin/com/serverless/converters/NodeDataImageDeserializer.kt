package com.serverless.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElement
import com.workduck.utils.Helper

class NodeDataImageDeserializer : StdConverter<Map<String, String>, MutableList<AdvancedElement>>()  {
    override fun convert(nodeData: Map<String, String>): MutableList<AdvancedElement> {
        val listOfElements: MutableList<AdvancedElement> = mutableListOf()
        for ((_, v) in nodeData) {
            val element: AdvancedElement = Helper.objectMapper.readValue(v)
            listOfElements += element
        }
        return listOfElements
    }
}
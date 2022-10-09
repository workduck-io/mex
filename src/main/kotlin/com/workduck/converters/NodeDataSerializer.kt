package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.AdvancedElement
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class NodeDataSerializer : StdConverter<MutableList<AdvancedElement>, MutableMap<String, String>>(){
    override fun convert(nodeDataList: MutableList<AdvancedElement>): MutableMap<String, String> {
        val mapOfElements: MutableMap<String, String> = mutableMapOf()
        for (nodeData in nodeDataList) {
            val nodeID = nodeData.id
            mapOfElements.set(nodeID, Helper.objectMapper.writeValueAsString(nodeData))
        }
        return mapOfElements
    }
}
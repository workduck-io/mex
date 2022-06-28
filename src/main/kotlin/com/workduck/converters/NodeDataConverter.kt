package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElement
import com.workduck.utils.Helper

class NodeDataConverter : DynamoDBTypeConverter<MutableMap<String, String>, MutableList<AdvancedElement>> {

    private val objectMapper = Helper.objectMapper

    override fun convert(nodeData: MutableList<AdvancedElement>): MutableMap<String, String> {
        val mapOfData: MutableMap<String, String> = mutableMapOf()
        for (element in nodeData) {
            mapOfData[element.id] = objectMapper.writeValueAsString(element)
        }
        return mapOfData
    }

    override fun unconvert(nodeData: MutableMap<String, String>): MutableList<AdvancedElement> {
        val listOfElements: MutableList<AdvancedElement> = mutableListOf()
        for ((_, v) in nodeData) {
            val element: AdvancedElement = objectMapper.readValue(v)
            listOfElements += element
        }
        return listOfElements
    }
}

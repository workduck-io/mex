package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElement

class NodeDataConverter : DynamoDBTypeConverter<MutableMap<String, String>, MutableList<AdvancedElement>> {

    private val objectMapper = ObjectMapper()

    // override fun convert()
    override fun convert(n: MutableList<AdvancedElement>): MutableMap<String, String> {
        val mapOfData: MutableMap<String, String> = mutableMapOf()
        for (element in n) {
            mapOfData[element.getID()] = objectMapper.writeValueAsString(element)
        }
        return mapOfData
        // return objectMapper.writeValueAsString(n)
    }

    override fun unconvert(nodeData: MutableMap<String, String>): MutableList<AdvancedElement> {
        val listOfElements: MutableList<AdvancedElement> = mutableListOf()
        for ((_, v) in nodeData) {
            val element: AdvancedElement = objectMapper.readValue(v)
            listOfElements += element
        }
        return listOfElements
        // return objectMapper.readValue<List<Element>>(nodeData)
    }
}

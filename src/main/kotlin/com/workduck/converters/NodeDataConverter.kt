package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElement

class NodeDataConverter : DynamoDBTypeConverter<MutableMap<String, String>, MutableMap<String, AdvancedElement>> {

    private val objectMapper = ObjectMapper()

    override fun convert(n: MutableMap<String, AdvancedElement>): MutableMap<String, String> {
        val mapOfData: MutableMap<String, String> = mutableMapOf()
        for ((id,element) in n) {
            mapOfData[id] = objectMapper.writeValueAsString(element)
        }
        return mapOfData
        // return objectMapper.writeValueAsString(n)
    }

    override fun unconvert(nodeData: MutableMap<String, String>): MutableMap<String, AdvancedElement> {
        val mapOfElements: MutableMap<String, AdvancedElement> = mutableMapOf()
        for ((k, v) in nodeData) {
            val element: AdvancedElement = objectMapper.readValue(v)
            mapOfElements[k] = element
        }
        return mapOfElements
        // return objectMapper.readValue<List<Element>>(nodeData)
    }
}

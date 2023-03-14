package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.BlockElement
import com.workduck.utils.Helper

class CaptureDataConverter: DynamoDBTypeConverter<MutableMap<String, String>, MutableList<BlockElement>> {
    private val objectMapper = Helper.objectMapper

    override fun convert(nodeData: MutableList<BlockElement>): MutableMap<String, String> {
        val mapOfData: MutableMap<String, String> = mutableMapOf()
        for (element in nodeData) {
            mapOfData[element.captureId] = objectMapper.writeValueAsString(element)
        }
        return mapOfData
    }

    override fun unconvert(nodeData: MutableMap<String, String>): MutableList<BlockElement> {
        val listOfElements: MutableList<BlockElement> = mutableListOf()
        for ((_, v) in nodeData) {
            val element: BlockElement = objectMapper.readValue(v)
            listOfElements += element
        }
        return listOfElements
    }
}
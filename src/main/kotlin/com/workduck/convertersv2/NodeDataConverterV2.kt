package com.workduck.convertersv2

import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElement
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class NodeDataConverterV2 : AttributeConverter<List<AdvancedElement>> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(listOfAdvancedElement: List<AdvancedElement>): AttributeValue {

        val mapOfData: MutableMap<String, AttributeValue> = mutableMapOf()
        for (element in listOfAdvancedElement) {
            mapOfData[element.id] = AttributeValue.builder().s(objectMapper.writeValueAsString(element)).build()
        }

        return AttributeValue.builder().m(mapOfData).build()
    }

    override fun transformTo(attributeMap: AttributeValue): List<AdvancedElement> {
        val mapOfData = attributeMap.m()
        val listOfElements: MutableList<AdvancedElement> = mutableListOf()
        for ((_, v) in mapOfData) {
            val element: AdvancedElement = objectMapper.readValue(v.s())
            listOfElements += element
        }
        return listOfElements
    }

    override fun type(): EnhancedType<List<AdvancedElement>> {
        return EnhancedType.listOf(AdvancedElement::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.M
    }
}
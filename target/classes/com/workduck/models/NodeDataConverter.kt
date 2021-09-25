package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue



class NodeDataConverter : DynamoDBTypeConverter<String, List<Element>> {

	private val objectMapper = ObjectMapper()

	override fun convert(n : List<Element>): String? {
		return objectMapper.writeValueAsString(n)
		//val itemDimensions: List<Element> = n as List<Element>
//		var nodeData : String = ""
//		for(e in n) {
//			nodeData += String.format("id:%s x content:%s x type:%s x children: %s", e.getID(), e.content(), e.getElementType(), e.getChildren())
//		}
//		return nodeData


	}

	override fun unconvert(nodeData: String): List<Element> {
		return objectMapper.readValue<List<Element>>(nodeData)

	}

}
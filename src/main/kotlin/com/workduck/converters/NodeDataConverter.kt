package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Element


class NodeDataConverter : DynamoDBTypeConverter<MutableList<String>, MutableList<Element>> {

	private val objectMapper = ObjectMapper()

	//override fun convert()
	override fun convert(n : MutableList<Element>): MutableList<String> {
		val listOfData : MutableList<String> = mutableListOf()
		for(element in n){
			val e : String = objectMapper.writeValueAsString(element)
			listOfData += e
		}
		return listOfData
		//return objectMapper.writeValueAsString(n)

	}

	override fun unconvert(nodeData: MutableList<String>): MutableList<Element> {
		val listOfElements : MutableList<Element> = mutableListOf()
		for(string in nodeData){
			val element : Element = objectMapper.readValue(string)
			listOfElements += element
		}
		return listOfElements
		//return objectMapper.readValue<List<Element>>(nodeData)

	}

}
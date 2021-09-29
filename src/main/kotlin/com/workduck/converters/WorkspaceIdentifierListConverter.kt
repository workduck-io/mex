package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.WorkspaceIdentifier

class WorkspaceIdentifierListConverter : DynamoDBTypeConverter<MutableList<String>, MutableList<WorkspaceIdentifier>> {

	private val objectMapper = ObjectMapper()

	override fun convert(l : MutableList<WorkspaceIdentifier>): MutableList<String> {
		val listOfIdentifiers : MutableList<String> = mutableListOf()
		for(identifier in l){
			val i : String = objectMapper.writeValueAsString(identifier)
			listOfIdentifiers += i
		}
		return listOfIdentifiers

	}

	override fun unconvert(l: MutableList<String>): MutableList<WorkspaceIdentifier> {
		val listOfIdentifiers : MutableList<WorkspaceIdentifier> = mutableListOf()
		for(identifier in l){
			val i : WorkspaceIdentifier = objectMapper.readValue(identifier)
			listOfIdentifiers += i
		}
		return listOfIdentifiers

	}

}
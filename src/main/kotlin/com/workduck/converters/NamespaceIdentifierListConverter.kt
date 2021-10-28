package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NamespaceIdentifier

/* Not being used anywhere as of now */
class NamespaceIdentifierListConverter : DynamoDBTypeConverter<MutableList<String>, MutableList<NamespaceIdentifier>> {

    private val objectMapper = ObjectMapper()

    override fun convert(l: MutableList<NamespaceIdentifier>): MutableList<String> {
        val listOfIdentifiers: MutableList<String> = mutableListOf()
        for (identifier in l) {
            val i: String = objectMapper.writeValueAsString(identifier)
            listOfIdentifiers += i
        }
        return listOfIdentifiers
    }

    override fun unconvert(l: MutableList<String>): MutableList<NamespaceIdentifier> {
        val listOfIdentifiers: MutableList<NamespaceIdentifier> = mutableListOf()
        for (identifier in l) {
            val i: NamespaceIdentifier = objectMapper.readValue(identifier)
            listOfIdentifiers += i
        }
        return listOfIdentifiers
    }
}

package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.NodeSchemaIdentifier

class NodeSchemaIdentifierConverter : DynamoDBTypeConverter<String, NodeSchemaIdentifier?> {

    private val objectMapper = ObjectMapper()

    override fun convert(nodeSchemaIdentifier: NodeSchemaIdentifier?): String? = nodeSchemaIdentifier?.id


    override fun unconvert(nodeSchemaID: String): NodeSchemaIdentifier = NodeSchemaIdentifier(nodeSchemaID)

}

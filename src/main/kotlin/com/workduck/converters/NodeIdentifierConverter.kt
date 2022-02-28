package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.NodeIdentifier

class NodeIdentifierConverter: DynamoDBTypeConverter<String, NodeIdentifier?> {

    override fun convert(node: NodeIdentifier?): String? =  node?.id


    override fun unconvert(nodeID: String): NodeIdentifier = NodeIdentifier(nodeID)

}
package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.NodeIdentifier

class NodeIdentifierListConverter: DynamoDBTypeConverter<MutableMap<String, String>,  MutableList<NodeIdentifier>> {

    override fun convert(nodes: MutableList<NodeIdentifier>): MutableMap<String, String> {
        val mapOfNodeIDs: MutableMap<String, String> = mutableMapOf()
        for (node in nodes) {
            mapOfNodeIDs[node.id] = node.id
        }
        return mapOfNodeIDs
    }

    override fun unconvert(nodeData: MutableMap<String, String>):  MutableList<NodeIdentifier> {
        val listOfNodes: MutableList<NodeIdentifier> = mutableListOf()
        for ((_, nodeID) in nodeData) {
            listOfNodes += NodeIdentifier(nodeID)
        }
        return listOfNodes
    }
}

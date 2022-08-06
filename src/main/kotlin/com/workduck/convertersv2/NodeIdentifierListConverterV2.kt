package com.workduck.convertersv2

import com.workduck.models.NodeIdentifier
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class NodeIdentifierListConverterV2 : AttributeConverter<List<NodeIdentifier>> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(nodes: List<NodeIdentifier>): AttributeValue {

        val mapOfNodeIDs: MutableMap<String, AttributeValue> = mutableMapOf()
        for (node in nodes) {
            mapOfNodeIDs[node.id] = AttributeValue.builder().s(node.id).build()
        }
        return AttributeValue.builder().m(mapOfNodeIDs).build()


    }

    override fun transformTo(mapOfNodeIDToNodeID: AttributeValue): List<NodeIdentifier> {
        val listOfNodes: MutableList<NodeIdentifier> = mutableListOf()
        for (nodeID in mapOfNodeIDToNodeID.m().values) {
            listOfNodes += NodeIdentifier(nodeID.s())
        }
        return listOfNodes.toList()
    }

    override fun type(): EnhancedType<List<NodeIdentifier>> {
        return EnhancedType.listOf(NodeIdentifier::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.M
    }
}
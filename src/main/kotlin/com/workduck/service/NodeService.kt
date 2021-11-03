package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.workduck.models.*
import com.workduck.repositories.NodeRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper

/**
 * contains all node related logic
 */
class NodeService {
    // Todo: Inject them from handlers

    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()

    private val nodeRepository: NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig)
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, nodeRepository, dynamoDBMapperConfig)

    fun createNode(jsonString: String): Entity? {
        println("Should be created in the table : $tableName")
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val node: Node = objectMapper.readValue(jsonString)

        println(node)
        /* since idCopy is SK for Node object, it can't be null if not sent from frontend */
        node.idCopy = node.id
        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"

        node.dataOrder = createDataOrderForNode(node)
        node.createBy = node.lastEditedBy

        computeHashOfNodeData(node)

        for (e in node.data!!) {
            e.createdBy = node.lastEditedBy
            e.lastEditedBy = node.lastEditedBy
            e.createdAt = node.createdAt
            e.updatedAt = node.createdAt
        }

        return repository.create(node)
    }

    private fun createDataOrderForNode(node: Node): MutableList<String> {

        val list = mutableListOf<String>()
        for (element in node.data!!) {
            list += element.getID()
        }
        return list
    }

    fun getNode(nodeID: String): Entity? {
        return repository.get(NodeIdentifier(nodeID))
    }

    fun deleteNode(nodeID: String): Identifier? {
        return repository.delete(NodeIdentifier(nodeID))
    }

    fun append(nodeID: String, jsonString: String): Map<String, Any>? {

        val objectMapper = ObjectMapper().registerKotlinModule()
        val elements: MutableList<AdvancedElement> = objectMapper.readValue(jsonString)

        val orderList = mutableListOf<String>()
        var userID: String = ""
        for (e in elements) {
            orderList += e.getID()

            e.lastEditedBy = e.createdBy
            e.createdAt = System.currentTimeMillis()
            e.updatedAt = e.createdAt
            userID = e.createdBy as String
        }
        return nodeRepository.append(nodeID, userID, elements, orderList)
    }

    fun updateNode(jsonString: String): Entity? {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val node: Node = objectMapper.readValue(jsonString)

        /* since idCopy is SK for Node object, it can't be null if not sent from frontend */
        node.idCopy = node.id

        /* createdAt should not be updated in updateNode flow */
        node.createdAt = null


        /* In case workspace/ namespace have been updated, AK needs to be updated as well */
        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"

        node.dataOrder = createDataOrderForNode(node)

        computeHashOfNodeData(node)

        val storedNode: Node = getNode(node.id) as Node

        /* to update block level details for accountability */
        compareNodeWithStoredNode(node, storedNode)

        /* to make the versions same */
        mergeNodeVersions(node, storedNode)

        return nodeRepository.update(node)
    }

    fun getAllNodesWithWorkspaceID(workspaceID: String): MutableList<String>? {

        return nodeRepository.getAllNodesWithWorkspaceID(workspaceID)
    }

    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): MutableList<String>? {

        return nodeRepository.getAllNodesWithNamespaceID(namespaceID, workspaceID)
    }

    fun updateNodeBlock(nodeID: String, blockJson: String): AdvancedElement? {

        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val element: AdvancedElement = objectMapper.readValue(blockJson)

        val blockData = objectMapper.writeValueAsString(element)

        return nodeRepository.updateNodeBlock(nodeID, blockData, element.getID(), element.lastEditedBy as String)
    }

    private fun computeHashOfNodeData(node: Node) {
        for (e in node.data!!) {
            val clonedElement: AdvancedElement = e
            clonedElement.createdAt = null
            clonedElement.updatedAt = null
            clonedElement.lastEditedBy = null
            clonedElement.createdBy = null
            clonedElement.hashCode = null
            e.hashCode = clonedElement.hashCode()
        }
    }

    private fun mergeNodeVersions(node: Node, storedNode: Node) {

        /* currently just handling when more blocks have been added */

        /* not handling the case when
            1. same block(s) has/have been edited
            2. some blocks deleted either by user1 or user2
        */
        val storedNodeDataOrder = storedNode.dataOrder
        val sentDataOrder = node.dataOrder
        val finalDataOrder = mutableListOf<String>()
        for ((index, nodeID) in storedNodeDataOrder!!.withIndex()) {
            if (nodeID == sentDataOrder!![index]) {
                finalDataOrder.add(nodeID)
            } else {
                if (sentDataOrder[index] !in finalDataOrder)
                    finalDataOrder.add(sentDataOrder[index])

                if (nodeID !in finalDataOrder)
                    finalDataOrder.add(nodeID)
            }
        }

        var remaining = storedNodeDataOrder.size
        while (remaining < sentDataOrder!!.size) {
            if (sentDataOrder[remaining] !in finalDataOrder)
                finalDataOrder.add(sentDataOrder[remaining])
            remaining++
        }

        node.dataOrder = finalDataOrder
        node.version = storedNode.version
    }

    private fun compareNodeWithStoredNode(node: Node, storedNode: Node) {
        for (currElement in node.data!!) {
            var isPresent = false
            for (storedElement in storedNode.data!!) {
                if (storedElement.getID() == currElement.getID()) {
                    isPresent = true

                    /* if the block has not been updated */
                    if (storedElement.hashCode == currElement.hashCode) {
                        currElement.createdAt = storedElement.createdAt
                        currElement.updatedAt = storedElement.updatedAt
                        currElement.createdBy = storedElement.createdBy
                        currElement.lastEditedBy = storedElement.lastEditedBy
                    }

                    /* when the block has been updated */
                    else {
                        currElement.createdAt = storedElement.createdAt
                        currElement.updatedAt = System.currentTimeMillis()
                        currElement.createdBy = storedElement.createdBy
                        currElement.lastEditedBy = node.lastEditedBy
                    }
                }
            }

            if (!isPresent) {
                currElement.createdAt = node.updatedAt
                currElement.updatedAt = node.updatedAt
                currElement.createdBy = node.lastEditedBy
                currElement.lastEditedBy = node.lastEditedBy
            }
        }
    }
}

fun main() {
    val jsonString: String = """
		{
            "lastEditedBy" : "Varun",
			"id": "NODE1",
            "namespaceIdentifier" : "NAMESPACE1",
            "workspaceIdentifier" : "WORKSPACE1",
			"data": [
			{
				"id": "sampleParentID",
                "elementType": "list",
                "childrenElements": [
                {
                    "id" : "sampleChildID",
                    "content" : "sample child content",
                    "elementType": "list",
                    "properties" :  { "bold" : true, "italic" : true  }
                }
                ]
			},
            {
				"id": "1234",
                "elementType": "list",
                "childrenElements": [
                {
                    "id" : "sampleChildID",
                    "content" : "sample child content",
                    "elementType": "list",
                    "properties" :  { "bold" : true, "italic" : true  }
                }
                ]
			}
			]
		}
		"""

    val jsonString1: String = """
        
    {
        "lastEditedBy" : "Ruddhi",
        "id": "NODE1",
        "namespaceIdentifier" : "NAMESPACE1",
        "workspaceIdentifier" : "WORKSPACE1",
        "data": [
        {
            "id": "sampleParentID",
            "elementType": "list",
            "childrenElements": [
            {
                "id" : "sampleChildID",
                "content" : "sample child content 1",
                "elementType": "list",
                "properties" :  { "bold" : true, "italic" : true  }
            }
            ]
        },
        {
            "id": "sampleParentID2",
            "elementType": "list",
            "childrenElements": [
            {
                "id" : "sampleChildID2",
                "content" : "sample child content",
                "elementType": "list",
                "properties" :  { "bold" : true, "italic" : true  }
            }
            ]
        },
        {
            "id": "1234",
            "elementType": "list",
            "childrenElements": [
            {
                "id" : "sampleChildID",
                "content" : "sample child content",
                "elementType": "list",
                "properties" :  { "bold" : true, "italic" : true  }
            }
            ]
        }
        ]
    }
    """

    val jsonForAppend: String = """
        [
            {
            "createdBy" : "Varun",
            "id": "xyz",
            "content": "Sample Content 4",
            "elementType" : "list",
            "childrenElements": [
            {
               
                "id" : "sampleChildID4",
                "content" : "sample child content"
            }
            ]},
            {
            "createdBy" : "Varun",
            "id": "abc",
            "content": "Sample Content 5",
            "elementType" : "random element type",
            "childrenElements": [
            {
                "id" : "sampleChildID5",
                "content" : "sample child content"
            }
            ]}
            
        ]
        """

    val jsonForEditBlock = """
        {
            "id" : "sampleParentID",
            "elementType": "list",
            "childrenElements": [
              {
                  "id" : "sampleChildID",
                  "content" : "edited child content - direct set - second try",
                  "elementType": "list",
                  "properties" :  { "bold" : true, "italic" : true  }
              }
                ]
        }
      """

    // NodeService().createNode(jsonString)
    // println(NodeService().getNode("NODE1"))
    NodeService().updateNode(jsonString1)
    // NodeService().deleteNode("NODEF873GEFPVJQKV43NQMWQEJQGLF")
    // NodeService().jsonToObjectMapper(jsonString1)
    // NodeService().jsonToElement()
    // NodeService().append("NODE1",jsonForAppend)
    // println(System.getenv("PRIMARY_TABLE"))
    // println(NodeService().getAllNodesWithNamespaceID("NAMESPACE1", "WORKSPACE1"))
    // NodeService().updateNodeBlock("NODE1", jsonForEditBlock)

    // NodeService().testOrderedMap()
    // println(NodeService().getAllNodesWithWorkspaceID("WORKSPACE1"))
    // TODO("for list of nodes, I should be getting just namespace/workspace IDs and not the whole serialized object")
}

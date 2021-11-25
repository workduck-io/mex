package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.NodeRequest
import com.serverless.models.WDRequest
import com.workduck.models.Node
import com.workduck.models.Entity
import com.workduck.models.NodeIdentifier
import com.workduck.models.Identifier
import com.workduck.models.AdvancedElement
import com.workduck.repositories.NodeRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

/**
 * contains all node related logic
 */
class NodeService {
    // Todo: Inject them from handlers

    private val objectMapper = Helper.objectMapper
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

    fun createNode(node: Node): Entity? {
        LOG.info("Should be created in the table : $tableName")


        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"
        node.dataOrder = createDataOrderForNode(node)
        node.createBy = node.lastEditedBy

        //computeHashOfNodeData(node)

        for (e in node.data!!) {
            e.createdBy = node.lastEditedBy
            e.lastEditedBy = node.lastEditedBy
            e.createdAt = node.createdAt
            e.updatedAt = node.createdAt
        }

        LOG.info("Creating node : $node")

        return repository.create(node)
    }

    fun createAndUpdateNode(nodeRequest: WDRequest?) : Entity? {
        val node : Node = createNodeObjectFromNodeRequest(nodeRequest as NodeRequest?) ?: return null

        val storedNode = getNode(node.id) as Node?

        return if(storedNode == null){
            createNode(node)
        }
        else{
            updateNode(node, storedNode)
        }
    }

    private fun createDataOrderForNode(node: Node): MutableList<String> {

        val list = mutableListOf<String>()
        for (element in node.data!!) {
            list += element.id
        }
        return list
    }

    fun getNode(nodeID: String, bookmarkInfo : Boolean? = null, userID : String? = null): Entity? {


        val node =  repository.get(NodeIdentifier(nodeID)) as Node?
        println("USER ID $userID")
        if(bookmarkInfo == true && userID != null){
            node?.isBookmarked = UserBookmarkService().isNodeBookmarkedForUser(nodeID, userID)
        }

        return node

    }

    fun deleteNode(nodeID: String): Identifier? {
        LOG.info("Deleting node with id : $nodeID")
        return repository.delete(NodeIdentifier(nodeID))
    }

    fun append(nodeID: String, jsonString: String): Map<String, Any>? {

        val elements: MutableList<AdvancedElement> = objectMapper.readValue(jsonString)

        val orderList = mutableListOf<String>()
        var userID: String = ""
        for (e in elements) {
            orderList += e.id

            e.lastEditedBy = e.createdBy
            e.createdAt = System.currentTimeMillis()
            e.updatedAt = e.createdAt
            userID = e.createdBy as String
        }
        return nodeRepository.append(nodeID, userID, elements, orderList)
    }

    fun updateNode(node : Node, storedNode: Node): Entity? {

        /* set idCopy = id, createdAt = null, and set AK */
        Node.populateNodeWithSkAkAndCreatedAtNull(node)

        node.dataOrder = createDataOrderForNode(node)

        /* to update block level details for accountability */
        val nodeChanged : Boolean = compareNodeWithStoredNode(node, storedNode)

        if(!nodeChanged){
            return storedNode
        }

        /* to make the versions same */
        mergeNodeVersions(node, storedNode)

        LOG.info("Updating node : $node")
        return nodeRepository.update(node)
    }

    fun getAllNodesWithWorkspaceID(workspaceID: String): MutableList<String>? {

        return nodeRepository.getAllNodesWithWorkspaceID(workspaceID)
    }

    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): MutableList<String>? {

        return nodeRepository.getAllNodesWithNamespaceID(namespaceID, workspaceID)
    }

    fun updateNodeBlock(nodeID: String, blockJson: String): AdvancedElement? {

        val element: AdvancedElement = objectMapper.readValue(blockJson)

        val blockData = objectMapper.writeValueAsString(element)

        return nodeRepository.updateNodeBlock(nodeID, blockData, element.id, element.lastEditedBy as String)
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

        if(storedNodeDataOrder != null) {
            for ((index, nodeID) in storedNodeDataOrder.withIndex()) {
                if(sentDataOrder != null) {
                    if (nodeID == sentDataOrder[index]) {
                        finalDataOrder.add(nodeID)
                    } else {
                        if (sentDataOrder[index] !in finalDataOrder)
                            finalDataOrder.add(sentDataOrder[index])

                        if (nodeID !in finalDataOrder)
                            finalDataOrder.add(nodeID)
                    }
                }
            }
        }

        var remaining = storedNodeDataOrder?.size
        if (remaining != null && sentDataOrder != null) {
            while (remaining < sentDataOrder.size) {
                if (sentDataOrder[remaining] !in finalDataOrder)
                    finalDataOrder.add(sentDataOrder[remaining])
                remaining++
            }
        }

        node.dataOrder = finalDataOrder
        node.version = storedNode.version
    }

    private fun compareNodeWithStoredNode(node: Node, storedNode: Node) : Boolean{
        var nodeChanged = false
        if (node.data != null) {
            for (currElement in node.data!!) {
                var isPresent = false
                if(storedNode.data != null) {
                    for (storedElement in storedNode.data!!) {
                        if (storedElement.id == currElement.id) {
                            isPresent = true

                            /* if the block has not been updated */
                            if (currElement == storedElement) {
                                currElement.createdAt = storedElement.createdAt
                                currElement.updatedAt = storedElement.updatedAt
                                currElement.createdBy = storedElement.createdBy
                                currElement.lastEditedBy = storedElement.lastEditedBy
                            }

                            /* when the block has been updated */
                            else {
                                nodeChanged = true
                                currElement.createdAt = storedElement.createdAt
                                currElement.updatedAt = System.currentTimeMillis()
                                currElement.createdBy = storedElement.createdBy
                                currElement.lastEditedBy = node.lastEditedBy
                            }
                        }
                    }

                    if (!isPresent) {
                        nodeChanged = true
                        currElement.createdAt = node.updatedAt
                        currElement.updatedAt = node.updatedAt
                        currElement.createdBy = node.lastEditedBy
                        currElement.lastEditedBy = node.lastEditedBy
                    }
                }

            }
        }
        return nodeChanged
    }

    private fun createNodeObjectFromNodeRequest(nodeRequest: NodeRequest?) : Node? {
        return nodeRequest?.let{
            Node(id = nodeRequest.id,
                namespaceIdentifier = nodeRequest.namespaceIdentifier,
                workspaceIdentifier = nodeRequest.workspaceIdentifier,
                lastEditedBy = nodeRequest.lastEditedBy,
                data = nodeRequest.data)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeService::class.java)
    }

}

fun main() {
    val jsonString: String = """
		{
            "type" : "NodeRequest",
            "lastEditedBy" : "Varun",
			"id": "NODE1",
            "namespaceIdentifier" : "NAMESPACE1",
            "workspaceIdentifier" : "WORKSPACE1",
			"data": [
			{
				"id": "sampleParentID",
                "elementType": "list",
                "children": [
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
                "children": [
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
        "type" : "NodeRequest",
        "lastEditedBy" : "Ruddhi",
        "id": "NODE1",
        "namespaceIdentifier" : "NAMESPACE1",
        "workspaceIdentifier" : "WORKSPACE1",
        "data": [
        {
            "id": "sampleParentID",
            "elementType": "list",
            "children": [
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
            "children": [
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
                "children": [
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
            "children": [
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
            "children": [
            {
                "id" : "sampleChildID5",
                "content" : "sample child content"
            }
            ]}
            
        ]
        """

    val jsonForEditBlock = """
        {
            "lastEditedBy" : "Varun",
            "id" : "sampleParentID",
            "elementType": "list",
            "children": [
              {
                  "id" : "sampleChildID",
                  "content" : "edited child content - direct set - second tryy",
                  "elementType": "list",
                  "properties" :  { "bold" : true, "italic" : true  }
              }
                ]
        }
      """

    val nodeRequest = ObjectMapper().readValue<NodeRequest>(jsonString1)
     NodeService().createAndUpdateNode(nodeRequest)
    // println(NodeService().getNode("NODE2"))
    // NodeService().updateNode(jsonString1)
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

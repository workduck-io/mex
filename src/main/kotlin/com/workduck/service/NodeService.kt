package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.ElementRequest
import com.serverless.models.NodeRequest
import com.serverless.models.WDRequest
import com.workduck.models.*
import com.workduck.repositories.NodeRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import org.apache.logging.log4j.LogManager
import com.workduck.utils.Helper


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

    private val nodeRepository: NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig, client)
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, nodeRepository, dynamoDBMapperConfig)

    fun createNode(node: Node, versionEnabled: Boolean): Entity? {
        LOG.info("Should be created in the table : $tableName")


        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"
        node.dataOrder = createDataOrderForNode(node)
        node.createBy = node.lastEditedBy
        node.lastVersionCreatedAt = node.createdAt

        //computeHashOfNodeData(node)

        for (e in node.data!!) {
            e.createdBy = node.lastEditedBy
            e.lastEditedBy = node.lastEditedBy
            e.createdAt = node.createdAt
            e.updatedAt = node.createdAt
        }

        LOG.info("Creating node : $node")

        return if(versionEnabled){
            val nodeVersion: NodeVersion = createNodeVersionFromNode(node)
            node.nodeVersionCount = 1
            nodeRepository.createNodeWithVersion(node, nodeVersion)
        }
        else{
            repository.create(node)
        }
    }

    private fun createNodeVersionFromNode(node: Node): NodeVersion {
        val nodeVersion = NodeVersion(
            id = "${node.id}#VERSION", lastEditedBy = node.lastEditedBy, createBy = node.createBy,
            data = node.data, dataOrder = node.dataOrder, createdAt = node.createdAt, ak = node.ak, namespaceIdentifier = node.namespaceIdentifier,
            workspaceIdentifier = node.workspaceIdentifier, updatedAt = "UPDATED_AT#${node.updatedAt}"
        )

        nodeVersion.version = Helper.generateId("version")

        return nodeVersion
    }

    fun createAndUpdateNode(nodeRequest: WDRequest?, versionEnabled : Boolean = false) : Entity? {
        val node : Node = createNodeObjectFromNodeRequest(nodeRequest as NodeRequest?) ?: return null

        val storedNode = getNode(node.id) as Node?

        return if(storedNode == null){
            createNode(node, versionEnabled)
        }
        else{
            updateNode(node, storedNode, versionEnabled)
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

    fun append(nodeID: String, elementsListRequest: WDRequest): Map<String, Any>? {

        val elementsListRequestConverted = elementsListRequest as ElementRequest
        val elements = elementsListRequestConverted.elements

        LOG.info(elements)

        val orderList = mutableListOf<String>()
        var userID = ""
        for (e in elements) {
                orderList += e.id

                e.lastEditedBy = e.createdBy
                e.createdAt = System.currentTimeMillis()
                e.updatedAt = e.createdAt
                userID = e.createdBy as String
            }
        return nodeRepository.append(nodeID, userID, elements, orderList)
    }

    fun updateNode(node : Node, storedNode: Node, versionEnabled: Boolean): Entity? {

        /* set idCopy = id, createdAt = null, and set AK */
        Node.populateNodeWithSkAkAndCreatedAtNull(node)

        node.dataOrder = createDataOrderForNode(node)

        /* to update block level details for accountability */
        val nodeChanged : Boolean = compareNodeWithStoredNode(node, storedNode)

        if(!nodeChanged){
            return storedNode
        }

        /* to make the locking versions same */
        mergeNodeVersions(node, storedNode)

        LOG.info("Updating node : $node")
        //return nodeRepository.update(node)

        if(versionEnabled){
            /* if the time diff b/w the latest version ( in version table ) and current node's updatedAt is < 5 minutes, don't create another version */
            if(node.updatedAt - storedNode.lastVersionCreatedAt!! < 300000) {
                node.lastVersionCreatedAt = storedNode.lastVersionCreatedAt
                return repository.update(node)
            }
            node.lastVersionCreatedAt = node.updatedAt
            checkNodeVersionCount(node, storedNode.nodeVersionCount)

            val nodeVersion = createNodeVersionFromNode(node)
            nodeVersion.createdAt = storedNode.createdAt
            nodeVersion.createBy = storedNode.createBy

            return nodeRepository.updateNodeWithVersion(node, nodeVersion)
        }
        else{
            return repository.update(node)
        }
    }


    fun checkNodeVersionCount(node: Node, storedNodeVersionCount: Long)  {
        if(storedNodeVersionCount < 25 ) {
            node.nodeVersionCount = storedNodeVersionCount + 1
            println(Thread.currentThread().id)
        }
        else {
            node.nodeVersionCount = storedNodeVersionCount + 1
            //GlobalScope.launch {
                //println("Thread ID inside coroutine scope : " + Thread.currentThread().id)

                //println("Thread ID inside launch : " + Thread.currentThread().id)
                setTTLForOldestVersion(node.id)
                //println("After delay")

            //}
            println("Hello") // main coroutine continues while a previous one is delayed
        }
    }



    private fun setTTLForOldestVersion(nodeID : String){

        /*returns first element from sorted updatedAts in ascending order */
        val oldestUpdatedAt = getMetaDataForActiveVersions(nodeID)?.get(0)

        println(oldestUpdatedAt)

        if(oldestUpdatedAt != null)
            nodeRepository.setTTLForOldestVersion(nodeID, oldestUpdatedAt)

    }

    fun getMetaDataForActiveVersions(nodeID : String) : MutableList<String>?{
        return nodeRepository.getMetaDataForActiveVersions(nodeID)
    }

    fun getAllNodesWithWorkspaceID(workspaceID: String): MutableList<String>? {

        return nodeRepository.getAllNodesWithWorkspaceID(workspaceID)
    }

    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): MutableList<String>? {

        return nodeRepository.getAllNodesWithNamespaceID(namespaceID, workspaceID)
    }

    fun updateNodeBlock(nodeID: String, elementsListRequest: WDRequest): AdvancedElement? {

        val elementsListRequestConverted = elementsListRequest as ElementRequest
        val element = elementsListRequestConverted.elements.let{ it[0] }

        element.updatedAt = System.currentTimeMillis()

        //TODO(since we directly set the block info, createdAt and createdBy get lost since we're not getting anything from ddb)
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

        // TODO(explore autoMerge cmd line)
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


    // println(NodeService().getNode("NODE1"))
    //println("HELLO")
//    runBlocking {
//        println("WORLD")
//        NodeService().updateNode(jsonString1)
//    }
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
    // NodeService().getMetaDataForActiveVersions("NODE1")

    //NodeService().setTTLForOldestVersion("NODE1")

    // NodeService().testOrderedMap()
    // println(NodeService().getAllNodesWithWorkspaceID("WORKSPACE1"))
    // TODO("for list of nodes, I should be getting just namespace/workspace IDs and not the whole serialized object")
}

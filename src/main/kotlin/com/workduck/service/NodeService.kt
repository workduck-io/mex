package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.WDRequest
import com.serverless.models.NodeRequest
import com.serverless.models.GenericListRequest
import com.serverless.models.ElementRequest
import com.workduck.models.Node
import com.workduck.models.NodeVersion
import com.workduck.models.NodeIdentifier
import com.workduck.models.Entity
import com.workduck.models.AdvancedElement
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

    fun createNode(node: Node): Entity? {
        LOG.info("Should be created in the table : $tableName")

        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"

        /* only when node is actually being created */
        node.createdBy = node.lastEditedBy

        for ((_, e) in node.data!!) {
            e.createdBy = node.lastEditedBy
            e.lastEditedBy = node.lastEditedBy
            e.createdAt = node.createdAt
            e.updatedAt = node.createdAt
        }

        LOG.info("Creating node : $node")

        return repository.create(node)

    }

    fun createNodeVersion(node : Node, isPreviousUpdateBySameUser : Boolean = false){

        val currentTime = System.currentTimeMillis()

        /* if the time difference b/w last version and current one is < 5min, don't create it given
            that both versions are being created via the same user */
        if(node.lastVersionCreatedAt != null && currentTime - node.lastVersionCreatedAt!! < 300000
                && isPreviousUpdateBySameUser ) {
            LOG.info("A version was recently created!")
            return
        }

        val nodeVersion: NodeVersion = createNodeVersionFromNode(node)
        LOG.info("Node Version in NodeService : $nodeVersion")

        /* If the last version was created within 5 minutes, skip creating a new version */

        node.lastVersionCreatedAt = currentTime
        node.nodeVersionCount += 1

        checkNodeVersionCount(node.id, node.nodeVersionCount)

        nodeRepository.createNodeVersion(node, nodeVersion)
    }

    private fun createNodeVersionFromNode(node: Node): NodeVersion {
        val nodeVersion = NodeVersion(
            id = "${node.id}#VERSION",  sk = "UPDATED_AT#${node.updatedAt}", lastEditedBy = node.lastEditedBy, createdBy = node.createdBy,
            data = node.data, dataOrder = node.dataOrder, nodeCreatedAt = node.createdAt, ak = node.ak, namespaceIdentifier = node.namespaceIdentifier,
            workspaceIdentifier = node.workspaceIdentifier
        )

        nodeVersion.version = Helper.generateId("version")

        return nodeVersion
    }

    fun createAndUpdateNode(nodeRequest: WDRequest?) : Entity? {
        val node : Node = createNodeObjectFromNodeRequest(nodeRequest as NodeRequest?) ?: return null

        node.dataOrder = getDataOrderFromMap(node.data)

        val storedNode = getNode(node.id) as Node?

        return if(storedNode == null){
            createNode(node)
        }
        else{
            updateNode(node, storedNode)
        }
    }


    fun getNode(nodeID: String, bookmarkInfo : Boolean? = null, userID : String? = null): Entity? {
        val node =  repository.get(NodeIdentifier(nodeID)) as Node?
        if(bookmarkInfo == true && userID != null){
            node?.isBookmarked = UserBookmarkService().isNodeBookmarkedForUser(nodeID, userID)
        }
        return node
    }


    /* basically archive the nodes */
    fun deleteNodes(nodeIDRequest: WDRequest) : MutableList<String>{
        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)
        LOG.info(nodeIDList)
        return nodeRepository.unarchiveOrArchiveNodes(nodeIDList, "ARCHIVED")
    }

    fun convertGenericRequestToList(genericRequest: GenericListRequest) : List<String>{
        return genericRequest.ids
    }


    fun append(nodeID: String, elementsListRequest: WDRequest): Map<String, Any>? {

        val elementsListRequestConverted = elementsListRequest as ElementRequest
        val elements = elementsListRequestConverted.elements

        LOG.info(elements)

        val orderList = getDataOrderFromList(elements)
        var userID = ""
        for (e in elements) {
                e.lastEditedBy = e.createdBy
                e.createdAt = System.currentTimeMillis()
                e.updatedAt = e.createdAt
                userID = e.createdBy as String
        }
        return nodeRepository.append(nodeID, userID, elements, orderList)
    }

    fun updateNode(node : Node, storedNode: Node): Entity? {
        /* set idCopy = id, createdAt = null, and set AK */
        Node.populateNodeWithSkAkAndCreatedAtNull(node, storedNode)

        /* to update block level details for accountability */
        val nodeChanged : Boolean = compareNodeWithStoredNode(node, storedNode)

        if(!nodeChanged){
            return storedNode
        }

        /* to make the locking versions same */
        mergeNodeVersions(node, storedNode)

        LOG.info("Updating node : $node")
        //return nodeRepository.update(node)

//        if(versionEnabled){
//            /* if the time diff b/w the latest version ( in version table ) and current node's updatedAt is < 5 minutes, don't create another version */
//            if(node.updatedAt - storedNode.lastVersionCreatedAt!! < 300000) {
//                node.lastVersionCreatedAt = storedNode.lastVersionCreatedAt
//                return repository.update(node)
//            }
//            node.lastVersionCreatedAt = node.updatedAt
//
//            node.nodeVersionCount = storedNode.nodeVersionCount + 1
//            checkNodeVersionCount(node.id, node.nodeVersionCount)
//
//            val nodeVersion = createNodeVersionFromNode(node)
//            nodeVersion.createdAt = storedNode.createdAt
//            nodeVersion.createdBy = storedNode.createdBy
//
//            return nodeRepository.updateNodeWithVersion(node, nodeVersion)
//        }

        return repository.update(node)

    }


    fun checkNodeVersionCount(nodeID: String, nodeVersionCount: Long)  {
        if(nodeVersionCount > 25) {
            setTTLForOldestVersion(nodeID)
        }
    }

    private fun getDataOrderFromMap(mp : Map<String, AdvancedElement>?) : MutableList<String>{
        val dataOrder : MutableList<String> = mutableListOf()
        mp?.map{
            dataOrder.add(it.key)
        }
        return dataOrder
    }

    private fun getDataOrderFromList(list : List <AdvancedElement>?) : MutableList<String>{
        val dataOrder : MutableList<String> = mutableListOf()
        list?.map{
            dataOrder.add(it.id)
        }
        return dataOrder
    }



    private fun setTTLForOldestVersion(nodeID : String){

        /*returns first element from sorted updatedAts in ascending order */
        val oldestUpdatedAt = getMetaDataForActiveVersions(nodeID)[0]

        LOG.info("Node Version for $nodeID Oldest Updated At : $oldestUpdatedAt")

        if(oldestUpdatedAt != null)
            nodeRepository.setTTLForOldestVersion(nodeID, oldestUpdatedAt)

    }

    fun getMetaDataForActiveVersions(nodeID : String) : MutableList<String?>{
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

        /* if the same user edited the node the last time, he can overwrite anything */
        if(node.lastEditedBy == storedNode.lastEditedBy){
            node.version = storedNode.version
            return
        }
        /* currently just handling when more blocks have been added */

        /* not handling the case when
            1. same block(s) has/have been edited
            2. some blocks deleted either by user1 or user2
        */
        val storedNodeDataOrder = storedNode.dataOrder
        val sentDataOrder = node.dataOrder
        val finalDataOrder = mutableListOf<String>()

        //very basic handling of maintaining rough order amongst blocks
        if(storedNodeDataOrder != null && sentDataOrder != null) {

            for(storedNodeID in storedNodeDataOrder){
                finalDataOrder.add(storedNodeID)
            }

            for (storedNodeID in storedNodeDataOrder) {
                for(sentNodeID in sentDataOrder) {
                    if (storedNodeID == sentNodeID && storedNodeID !in finalDataOrder) {
                        finalDataOrder.add(storedNodeID)
                    }
                }
            }

            for(sentNodeID in sentDataOrder){
                if(sentNodeID !in finalDataOrder) finalDataOrder.add(sentNodeID)
            }
        }

//        var remaining = storedNodeDataOrder?.size
//        if (remaining != null && sentDataOrder != null) {
//            while (remaining < sentDataOrder.size) {
//                if (sentDataOrder[remaining] !in finalDataOrder)
//                    finalDataOrder.add(sentDataOrder[remaining])
//                remaining++
//            }
//        }

        node.dataOrder = finalDataOrder
        node.version = storedNode.version

        // TODO(explore autoMerge cmd line)
    }

    private fun compareNodeWithStoredNode(node: Node, storedNode: Node) : Boolean{
        var nodeChanged = false

        /* in case a block has been deleted */
        if(node.data != storedNode.data) nodeChanged = true

        if (node.data != null) {
            for ((_,currElement) in node.data!!) {
                var isPresent = false
                if(storedNode.data != null) {
                    for ((_,storedElement) in storedNode.data!!) {
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
                data = nodeRequest.data,
                dataOrder = nodeRequest.dataOrder)
        }
    }

    fun getMetaDataOfAllArchivedNodesOfWorkspace(workspaceID : String) : MutableList<String>?{
        return nodeRepository.getAllArchivedNodesOfWorkspace(workspaceID)
    }


    fun unarchiveNodes(nodeIDRequest: WDRequest) : MutableList<String>{
        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)
        return nodeRepository.unarchiveOrArchiveNodes(nodeIDList, "ACTIVE")
    }

    fun deleteArchivedNodes(nodeIDRequest: WDRequest) : MutableList<String> {

        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)
        val deletedNodesList : MutableList<String> = mutableListOf()
        for(nodeID in nodeIDList) {
            repository.delete(NodeIdentifier(nodeID))?.also{
                deletedNodesList.add(it.id)
            }
        }
        return deletedNodesList
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeService::class.java)
    }


    fun makeNodePublic(nodeID: String) {
        nodeRepository.toggleNodePublicAccess(nodeID, 1)
    }

    fun makeNodePrivate(nodeID: String) {
        nodeRepository.toggleNodePublicAccess(nodeID, 0)
    }

    fun getPublicNode(nodeID: String) : Node?{
        return nodeRepository.getPublicNode(nodeID)
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
                "elementType": "paragraph",
                "children": [
                {
                    "id" : "sampleChildID",
                    "content" : "sample child content 1",
                    "elementType": "paragraph",
                    "properties" :  { "bold" : true, "italic" : true  }
                }
                ]
			},
            {
				"id": "bbbb1234",
                "elementType": "paragraph",
                "children": [
                {
                    "id" : "sampleChildID",
                    "content" : "sample child content",
                    "elementType": "paragraph",
                    "properties" :  { "bold" : true, "italic" : true  }
                }
                ]
			},
            {
				"id": "aasampleParentID",
                "elementType": "paragraph",
                "children": [
                {
                    "id" : "sampleChildID",
                    "content" : "sample child content 1",
                    "elementType": "paragraph",
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
            "id": "sampleParentID2",
            "elementType": "paragraph",
            "children": [
            {
                "id" : "sampleChildID",
                "content" : "sample child content 1",
                "elementType": "paragraph",
                "properties" :  { "bold" : true, "italic" : true  }
            }
            ]
        }]
        
    }
    """

    val jsonForAppend: String = """
        {
        "type" : "ElementRequest",
        "elements" : [
            {
            "createdBy" : "Varun",
            "id": "xyz",
            "content": "Sample Content 4",
            "elementType" : "list",
            "children": [
            {
               
                "id" : "sampleChildID4",
                "elementType": "paragraph",
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
                "elementType": "paragraph",
                "content" : "sample child content"
            }
            ]}
            
            ]
        }
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
    // NodeService().updateNode(jsonString1)
    // NodeService().deleteNode("NODE1")
    // NodeService().jsonToObjectMapper(jsonString1)
    // NodeService().jsonToElement()

    val appendBlock : WDRequest = ObjectMapper().readValue(jsonForAppend)
     NodeService().append("NODE1",appendBlock)
    // println(System.getenv("PRIMARY_TABLE"))
    // println(NodeService().getAllNodesWithNamespaceID("NAMESPACE1", "WORKSPACE1"))
    // NodeService().updateNodeBlock("NODE1", jsonForEditBlock)
    // NodeService().getMetaDataForActiveVersions("NODE1")

    //NodeService().setTTLForOldestVersion("NODE1")

    //NodeService().getMetaDataOfAllArchivedNodesOfWorkspace("WORKSPACE1")


    //    NodeService().makeNodePublic("NODE1")
    //NodeService().getPublicNode("NODE1")
    // NodeService().testOrderedMap()
    // println(NodeService().getAllNodesWithWorkspaceID("WORKSPACE1"))
    // TODO("for list of nodes, I should be getting just namespace/workspace IDs and not the whole serialized object")
}

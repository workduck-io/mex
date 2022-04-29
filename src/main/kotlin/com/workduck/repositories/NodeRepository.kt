package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTransactionWriteExpression
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Index
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes
import com.amazonaws.services.dynamodbv2.document.spec.BatchWriteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest
import com.amazonaws.services.dynamodbv2.model.TransactionCanceledException
import com.amazonaws.services.dynamodbv2.model.Update
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.utils.Constants
import com.serverless.utils.Constants.getCurrentTime
import com.workduck.models.AdvancedElement
import com.workduck.models.Comment
import com.workduck.models.Element
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.Node
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.NodeVersion
import com.workduck.models.Relationship
import com.workduck.models.RelationshipType
import com.workduck.service.NodeService
import com.workduck.utils.DDBHelper
import com.workduck.utils.DDBTransactionHelper
import com.workduck.utils.NodeHelper
import org.apache.logging.log4j.LogManager
import com.workduck.utils.Helper
import com.workduck.utils.RelationshipHelper.getRelationshipSK
import java.time.Instant


class NodeRepository(
    private val mapper: DynamoDBMapper,
    private val dynamoDB: DynamoDB,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig,
    private val client: AmazonDynamoDB,
    private val tableName: String,
    private val nodeService: NodeService
)  {

    fun getPaginatedNodeDataAndEndCursor(nodeID: String, relationships: List<Relationship>, blockID: String?, blockSize: Int, blocksProcessed : MutableList<Int> = mutableListOf(0)): Pair<String?, MutableList<AdvancedElement>> {

        val nodeData = mapper.load(Node::class.java, nodeID, nodeID, dynamoDBMapperConfig)

        var pair = processBlocks(nodeData, blockID, relationships, blockSize, blocksProcessed)

        if(blocksProcessed[0] == blockSize) return pair

        NodeHelper.getNodeNextNodeID(nodeID, relationships).let {
            when (it) {
                nodeID -> return pair /* case when there's no more relationships left */
                else -> {
                    val newData =  getPaginatedNodeDataAndEndCursor(it, relationships, null, blockSize, blocksProcessed)
                    pair.second += newData.second
                    pair = pair.copy(first = newData.first)
                }
            }
        }
        return pair
    }

    fun getPaginatedNodeDataReverseAndEndCursor(nodeID: String, relationships: List<Relationship>, blockID: String?, blockSize: Int, blocksProcessed : MutableList<Int> = mutableListOf(0)): Pair<String?, MutableList<AdvancedElement>> {

        val nodeData = mapper.load(Node::class.java, nodeID, nodeID, dynamoDBMapperConfig)

        var pair = processBlocksInReverse(nodeData, blockID, relationships, blockSize, blocksProcessed)

        if(blocksProcessed[0] == blockSize) return pair

        NodeHelper.getNodePreviousNodeID(nodeID, relationships).let {
            when (it) {
                nodeID -> return pair /* case when there's no more relationships left */
                else -> {
                    val newData = getPaginatedNodeDataReverseAndEndCursor(it, relationships, null, blockSize, blocksProcessed)
                    pair.second += newData.second
                    pair = pair.copy(first = newData.first)

                }
            }
        }

        return pair
    }


    private fun processBlocks(nodeData: Node, blockID: String?, relationships: List<Relationship>, blockSize: Int, blocksProcessed: MutableList<Int>) : Pair<String?, MutableList<AdvancedElement>>{

        val listOfElements = mutableListOf<AdvancedElement>()

        var indexOfStartBlock = nodeData.dataOrder?.indexOf(blockID) ?: 0

        if(indexOfStartBlock == -1) indexOfStartBlock = 0

        for (indexOfDataOrder in indexOfStartBlock until nodeData.dataOrder?.size!!) {
            for (element in nodeData.data!!) {
                if (element.id == nodeData.dataOrder?.get(indexOfDataOrder)) {
                    blocksProcessed[0]++
                    listOfElements.add(element)
                    if (blocksProcessed[0] == blockSize) {
                        return if(indexOfDataOrder + 1 < nodeData.data!!.size) Pair("${nodeData.id}#${nodeData.dataOrder?.get(indexOfDataOrder+1)}",listOfElements)
                        else {
                            val nextNodeID = NodeHelper.getNodeNextNodeID(nodeData.id, relationships).let{
                                if(it == nodeData.id) null
                                else it
                            }
                            Pair(nextNodeID , listOfElements)
                        }
                    }
                }
            }
        }

        return Pair(null, listOfElements)

    }

    private fun processBlocksInReverse(nodeData: Node, blockID: String?, relationships: List<Relationship>, blockSize: Int, blocksProcessed: MutableList<Int>) : Pair<String?, MutableList<AdvancedElement>>{

        val listOfElements = mutableListOf<AdvancedElement>()

        var indexOfStartBlock = nodeData.dataOrder?.indexOf(blockID) ?: (nodeData.dataOrder?.size!! - 1)

        if(indexOfStartBlock == -1) indexOfStartBlock = nodeData.dataOrder?.size!! - 1

        for (indexOfDataOrder in indexOfStartBlock downTo 0) {
            for (element in nodeData.data!!) {
                if (element.id == nodeData.dataOrder?.get(indexOfDataOrder)) {
                    blocksProcessed[0]++
                    listOfElements.add(element)
                    if (blocksProcessed[0] == blockSize) {
                        return if(indexOfDataOrder-1 >= 0) Pair("${nodeData.id}#${nodeData.dataOrder?.get(indexOfDataOrder-1)}",listOfElements)
                        else {
                            /* when the current node has exhausted all the blocks and blockSize is reached as well */
                            val prevNodeID = NodeHelper.getNodePreviousNodeID(nodeData.id, relationships).let{
                                if(it == nodeData.id) null
                                else it
                            }
                            Pair(prevNodeID, listOfElements)
                        }
                    }
                }
            }
        }

        return Pair(null, listOfElements)

    }


    fun getNodeElements(nodeID : String, relationships : List<Relationship>) : MutableList<AdvancedElement>?{
        LOG.info("Getting elements for $nodeID")

        val nextNodeID = NodeHelper.getNodeNextNodeID(nodeID, relationships)

        val nodeElements : MutableList<AdvancedElement>? = mapper.load(Node::class.java, nodeID, nodeID, dynamoDBMapperConfig).data?.let {
            return when(nodeID != nextNodeID){
                true -> {
                    getNodeElements(nextNodeID, relationships)?.let { it1 -> it.toMutableList().addAll(it1) }
                    it.toMutableList()
                }
                else -> it.toMutableList()
            }
        }

        return nodeElements


    }

    private fun orderBlocks(node: Node): Entity =
        node.apply {
            node.data?.let { data ->
                (
                    node.dataOrder?.mapNotNull { blockId ->
                        data.find { element -> blockId == element.id }
                    } ?: emptyList()
                    )
                    .also {
                        node.data = it.toMutableList()
                    }
            }
        }

    fun append(sourceNodeID: String, nodeID: String, workspaceID: String, userID: String, elements: List<AdvancedElement>, orderList: MutableList<String>) {
        LOG.info("Source Node : $sourceNodeID, Node to which we append : $nodeID")

        val table = dynamoDB.getTable(tableName)

        /* this is to ensure correct ordering of blocks/ elements */
        var updateExpression = "set dataOrder = list_append(if_not_exists(dataOrder, :empty_list), :orderList), lastEditedBy = :userID, updatedAt = :updatedAt"

        val objectMapper = ObjectMapper()

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()

        /* we build updateExpression to enable appending of multiple key value pairs to the map with just one query */
        for ((counter, e) in elements.withIndex()) {
            val entry: String = objectMapper.writeValueAsString(e)
            updateExpression += ", nodeData.${e.id} = :val$counter"
            expressionAttributeValues[":val$counter"] = AttributeValue(entry)
        }

        val currentTime = System.currentTimeMillis()
        val orderListAttributeValue = orderList.map { blockID -> AttributeValue().withS(blockID) }

        expressionAttributeValues[":userID"] = AttributeValue(userID)
        expressionAttributeValues[":updatedAt"] = AttributeValue().withN(currentTime.toString())
        expressionAttributeValues[":orderList"] = AttributeValue().withL(orderListAttributeValue)
        expressionAttributeValues[":empty_list"] = AttributeValue().withL()

        val nodeKey = HashMap<String, AttributeValue>()
        nodeKey["PK"] = AttributeValue(nodeID)
        nodeKey["SK"] = AttributeValue(nodeID)

        val appendItems : Update = Update()
                .withKey(nodeKey)
                .withTableName(tableName)
                .withUpdateExpression(updateExpression)
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConditionExpression("attribute_exists(PK) and attribute_exists(SK)")


       try{
            when(sourceNodeID == nodeID){
                true -> {
                    val actions: Collection<TransactWriteItem> = listOf(
                            TransactWriteItem().withUpdate(appendItems))

                    val placeOrderTransaction = TransactWriteItemsRequest()
                            .withTransactItems(actions)
                    client.transactWriteItems(placeOrderTransaction)

                }

                false -> {

                    val expressionAttributeValuesSourceNode: MutableMap<String, AttributeValue> = HashMap()

                    expressionAttributeValuesSourceNode[":userID"] = AttributeValue(userID)
                    expressionAttributeValuesSourceNode[":updatedAt"] = AttributeValue().withN(currentTime.toString())

                    val sourceNodeKey = HashMap<String, AttributeValue>()
                    sourceNodeKey["PK"] = AttributeValue(sourceNodeID)
                    sourceNodeKey["SK"] = AttributeValue(sourceNodeID)

                    val updateExpressionSource = "set lastEditedBy = :userID, updatedAt = :updatedAt"


                    val updatedAt : Update = Update()
                            .withKey(sourceNodeKey)
                            .withTableName(tableName)
                            .withUpdateExpression(updateExpressionSource)
                            .withExpressionAttributeValues(expressionAttributeValuesSourceNode)

                    val actions: Collection<TransactWriteItem> = listOf(
                            TransactWriteItem().withUpdate(appendItems),
                            TransactWriteItem().withUpdate(updatedAt))

                    val placeOrderTransaction = TransactWriteItemsRequest()
                            .withTransactItems(actions)
                    client.transactWriteItems(placeOrderTransaction)

                }
            }
        } catch (e: AmazonDynamoDBException) {
            if (e.errorMessage == "Item size to update has exceeded the maximum allowed size") {
                nodeService.createRelationship(sourceNodeID, nodeID, userID, elements, orderList)
            } else {
                throw e
            }
        }
    }

    fun createRelationshipAndNewNode(node: Node, relationship: Relationship): Boolean {
        LOG.info("Creating a relationship item : $relationship & a new node : $node")
        val transactionWriteRequest = TransactionWriteRequest()

        transactionWriteRequest.addPut(node)
        transactionWriteRequest.addPut(
            relationship,
            DynamoDBTransactionWriteExpression().withConditionExpression("attribute_not_exists(SK)")
        )

        return try {
            mapper.transactionWrite(transactionWriteRequest)
            LOG.info("Transaction successful")
            true
        } catch (error: TransactionCanceledException) {
            when (error.cancellationReasons.filter { it.code == "ConditionalCheckFailed" }.size) {
                0 -> throw error
                else -> return false /* case when two relationships from same start node are being formed */
            }
        }
    }

    fun getContainedRelationshipsForSourceNode(sourceNodeID: String): List<Relationship> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":ak"] = AttributeValue(sourceNodeID)
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.Relationship.name)
        expressionAttributeValues[":relationshipType"] = AttributeValue(RelationshipType.CONTAINED.name)

        return DynamoDBQueryExpression<Relationship>()
                .withKeyConditionExpression("AK = :ak  and itemType = :itemType")
                .withIndexName("itemType-AK-index").withConsistentRead(false)
                .withFilterExpression("typeOfRelationship = :relationshipType")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(Relationship::class.java, it, dynamoDBMapperConfig)
                }
    }

    fun getContainedRelationShipEndNode(sourceNodeID: String, startNodeID: String): String {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue("RLSP")
        expressionAttributeValues[":SK"] = AttributeValue(getRelationshipSK(startNodeID, RelationshipType.CONTAINED))
        expressionAttributeValues[":AK"] = AttributeValue(sourceNodeID)

        return DynamoDBQueryExpression<Relationship>()
                .withKeyConditionExpression("SK = :SK  and begins_with(PK, :PK)")
                .withIndexName("SK-PK-Index").withConsistentRead(false)
                .withFilterExpression("AK = :AK")
                .withExpressionAttributeValues(expressionAttributeValues).let { it ->
                mapper.query(Relationship::class.java, it, dynamoDBMapperConfig).first().endNode.id
            }
    }


    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): MutableList<String>? {

        val akValue = "$workspaceID${Constants.DELIMITER}$namespaceID"
        return try {
            DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(akValue, "itemType-AK-index", dynamoDB, "Node")
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    fun getAllNodesWithWorkspaceID(workspaceID: String): MutableList<String> {
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":PK"] = workspaceID
        expressionAttributeValues[":SK"] = ItemType.Node.name.uppercase()

        val items: ItemCollection<QueryOutcome?>? =  QuerySpec().withKeyConditionExpression("PK = :PK and begins_with(SK, :SK)")
                .withValueMap(expressionAttributeValues)
                .withProjectionExpression("SK")
                .let {
                    dynamoDB.getTable(tableName).query(it)
                }

        val iterator: Iterator<Item> = items!!.iterator()

        var itemList: MutableList<String> = mutableListOf()
        while (iterator.hasNext()) {
            val item: Item = iterator.next()
            itemList = (itemList + (item["PK"] as String)).toMutableList()
        }
        return itemList
    }

    fun getAllNodesWithUserID(userID: String): List<String> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":createdBy"] = AttributeValue(userID)
        expressionAttributeValues[":itemType"] = AttributeValue("Node")

        return DynamoDBQueryExpression<Node>()
            .withKeyConditionExpression("createdBy = :createdBy  and itemType = :itemType")
            .withIndexName("createdBy-itemType-index").withConsistentRead(false)
            .withProjectionExpression("PK")
            .withExpressionAttributeValues(expressionAttributeValues).let { it ->
                mapper.query(Node::class.java, it, dynamoDBMapperConfig).map { node ->
                    node.id
                }
            }
    }


    fun createMultipleNodes(listOfNodes : List<Node>){
        val failedBatches = mapper.batchWrite(listOfNodes, emptyList<Any>(), dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }

    fun createNodeWithVersion(node: Node, nodeVersion: NodeVersion): Node? {
        return try {
            val transactionWriteRequest = TransactionWriteRequest()
            transactionWriteRequest.addPut(node)
            transactionWriteRequest.addPut(nodeVersion)
            mapper.transactionWrite(transactionWriteRequest, dynamoDBMapperConfig)
            node
        } catch (e: Exception) {
            println(e)
            null
        }
    }


    fun updateNodeWithVersion(node: Node, nodeVersion: NodeVersion): Node? {
        val dynamoDBMapperUpdateConfig = DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build()

        return try {
            val transactionWriteRequest = TransactionWriteRequest()
            transactionWriteRequest.addUpdate(node)
            transactionWriteRequest.addPut(nodeVersion)
            DDBTransactionHelper(mapper).transactionWrite(transactionWriteRequest, dynamoDBMapperUpdateConfig, client)
            node
        } catch (e: ConditionalCheckFailedException) {
            /* Will happen only in race condition because we're making the versions same in the service */
            /* What should be the flow from here on? Call NodeService().update()? */
            LOG.info("Version mismatch!!")
            null
        } catch (e: java.lang.Exception) {
            LOG.info(e)
            null
        }
    }

    fun updateNodeBlock(nodeID: String, workspaceID: String, updatedBlock: String, blockID: String, userID: String): AdvancedElement? {
        val table = dynamoDB.getTable(tableName)
        val objectMapper = ObjectMapper()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedBlock"] = updatedBlock
        expressionAttributeValues[":userID"] = userID

        return UpdateItemSpec().withPrimaryKey("PK", workspaceID, "SK", nodeID)
            .withUpdateExpression("SET nodeData.$blockID = :updatedBlock, lastEditedBy = :userID ")
            .withValueMap(expressionAttributeValues)
            .withConditionExpression("attribute_exists(PK) and attribute_exists(SK)")
            .let {
                table.updateItem(it)
                objectMapper.readValue(updatedBlock)
            }
    }

    fun getNodeMetaData(nodeID: String): Node {
        LOG.info("Getting metadata for $nodeID")

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(nodeID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)

        val projectionExpression = "PK, SK, AK, workspaceIdentifier, namespaceIdentifier, nodeSchemaIdentifier, publicAccess, createdBy, lastEditedBy, tags, createdAt, updatedAt"

        return DynamoDBQueryExpression<Node>()
            .withKeyConditionExpression("PK = :pk and SK = :sk")
            .withExpressionAttributeValues(expressionAttributeValues)
            .withProjectionExpression(projectionExpression).let {
                mapper.query(Node::class.java, it, dynamoDBMapperConfig)[0]
            }
    }

    fun getMetaDataForActiveVersions(nodeID: String): MutableList<String>? {
        val table = dynamoDB.getTable(tableName)
        println("Inside getAllVersionsOfNode function")

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS("$nodeID${Constants.DELIMITER}VERSION")
        expressionAttributeValues[":status"] = AttributeValue().withS("ACTIVE")
        expressionAttributeValues[":NodeVersion"] = AttributeValue().withS("Node Version")

        val q = DynamoDBQueryExpression<NodeVersion>()
            .withKeyConditionExpression("PK = :pk")
            .withFilterExpression("versionStatus = :status and itemType = :NodeVersion")
            .withExpressionAttributeValues(expressionAttributeValues)
            .withProjectionExpression("SK")

        return try {
            val nodeVersionList: List<NodeVersion> = mapper.query(NodeVersion::class.java, q, dynamoDBMapperConfig)

            val itemList: MutableList<String> = mutableListOf()
            for (v in nodeVersionList) {
                if (v.updatedAt != null) itemList.add(v.updatedAt!!)
            }

            itemList
        } catch (e: Exception) {
            println(e)
            null
        }
    }

    fun setTTLForOldestVersion(nodeID: String, oldestUpdatedAt: String) {

        val table: Table = dynamoDB.getTable(tableName)

        val now: Long = Instant.now().epochSecond // unix time
        val ttl = (60).toLong()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":ttl"] = (now + ttl)
        expressionAttributeValues[":status"] = "INACTIVE"

        val u = UpdateItemSpec().withPrimaryKey("PK", "$nodeID${Constants.DELIMITER}VERSION", "SK", oldestUpdatedAt)
            .withUpdateExpression("SET timeToLive = :ttl, versionStatus = :status ")
            .withValueMap(expressionAttributeValues)

        try {
            table.updateItem(u)
        } catch (e: Exception) {
            println(e)
        }
    }

    fun unarchiveAndRenameNodes(mapOfNodeIDToName: Map<String, String>, workspaceID: String) : MutableList<String>{
        val table: Table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":active"] = ItemStatus.ACTIVE.name
        expressionAttributeValues[":updatedAt"] = getCurrentTime()

        val nodesProcessedList: MutableList<String> = mutableListOf()

        for ((nodeID,nodeName) in mapOfNodeIDToName) {
            try {
                expressionAttributeValues[":title"] = "$nodeName(1)"

                UpdateItemSpec().withPrimaryKey("PK", workspaceID, "SK", nodeID)
                        .withUpdateExpression("SET itemStatus = :active, title = :title, updatedAt = :updatedAt")
                        .withValueMap(expressionAttributeValues)
                        .withConditionExpression("attribute_exists(PK)")
                        .also {
                            table.updateItem(it)
                            nodesProcessedList += nodeID
                        }
            } catch (e: ConditionalCheckFailedException) {
                LOG.warn("nodeID : $nodeID not present in the DB")
            }
        }
        return nodesProcessedList
    }


    fun toggleNodePublicAccess(nodeID: String, accessValue: Long) {}

    fun getBlock(nodeID: String, blockID: String, workspaceID: String) : Node? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)

        val nodeList: List<Node> = DynamoDBQueryExpression<Node>()
            .withKeyConditionExpression("PK = :pk and SK = :sk")
            .withProjectionExpression("nodeData.$blockID, dataOrder")
            .withExpressionAttributeValues(expressionAttributeValues).let {
                mapper.query(Node::class.java, it)
            }

        return if (nodeList.isNotEmpty()) nodeList[0]
        else null
    }

    fun moveBlock(block: AdvancedElement?, workspaceID: String, sourceNodeID: String, destinationNodeID: String, dataOrderSourceNode: MutableList<String>) {

        val currentTime = getCurrentTime()

        val deleteBlock = getUpdateToDeleteBlockFromNode(block, workspaceID, sourceNodeID, dataOrderSourceNode, currentTime)
        val addBlock = getUpdateToAddBlockToNode(block, workspaceID, destinationNodeID, currentTime)

        val actions: Collection<TransactWriteItem> = listOf(
            TransactWriteItem().withUpdate(deleteBlock),
            TransactWriteItem().withUpdate(addBlock)
        )

        val moveBlockTransaction = TransactWriteItemsRequest().withTransactItems(actions)

        client.transactWriteItems(moveBlockTransaction)
    }

    private fun getUpdateToDeleteBlockFromNode(block: AdvancedElement?, workspaceID: String, nodeID: String, dataOrder: MutableList<String>, currentTime: Long): Update {

        val nodeKey = HashMap<String, AttributeValue>()
        nodeKey["PK"] = AttributeValue(workspaceID)
        nodeKey["SK"] = AttributeValue(nodeID)

        val expressionAttributeValues: MutableMap<String, AttributeValue> = mutableMapOf()

        val dataOrderList: MutableList<AttributeValue> = mutableListOf()

        dataOrder.map {
            dataOrderList.add(AttributeValue().withS(it))
        }

        expressionAttributeValues[":dataOrderNode1"] = AttributeValue().withL(dataOrderList)
        expressionAttributeValues[":updatedAt"] = AttributeValue().withN(currentTime.toString())

        val updateExpression1 = "remove nodeData.${block?.id} " +
            "set dataOrder = :dataOrderNode1, " +
            "updatedAt = :updatedAt"

        return Update().withTableName(tableName)
            .withKey(nodeKey)
            .withUpdateExpression(updateExpression1)
            .withExpressionAttributeValues(expressionAttributeValues)
    }


    private fun getUpdateToAddBlockToNode(block: AdvancedElement?, workspaceID: String, nodeID: String, currentTime: Long): Update{

        val nodeKey = HashMap<String, AttributeValue>()
        nodeKey["PK"] = AttributeValue(workspaceID)
        nodeKey["SK"] = AttributeValue(nodeID)

        val expressionAttributeValues: MutableMap<String, AttributeValue> = mutableMapOf()
        expressionAttributeValues[":updatedAt"] = AttributeValue().withN(currentTime.toString())
        expressionAttributeValues[":orderList"] = AttributeValue().withL(AttributeValue().withS(block?.id))
        expressionAttributeValues[":block"] = AttributeValue(Helper.objectMapper.writeValueAsString(block))

        val updateExpression = "set dataOrder = list_append(dataOrder, :orderList), " +
                "nodeData.${block?.id} = :block, updatedAt = :updatedAt"

        return Update().withTableName(tableName)
                        .withKey(nodeKey)
                        .withUpdateExpression(updateExpression)
                        .withExpressionAttributeValues(expressionAttributeValues)
    }

    fun renameNode(nodeID: String, newName: String, userID: String, workspaceID: String){
        LOG.info("$nodeID , new name : $newName")
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":title"] = newName
        expressionAttributeValues[":lastEditedBy"] = userID
        expressionAttributeValues[":updatedAt"] = getCurrentTime()

        try {
            UpdateItemSpec().withPrimaryKey("PK", workspaceID, "SK", nodeID)
                    .withUpdateExpression("SET title = :title, updatedAt = :updatedAt, lastEditedBy = :lastEditedBy")
                    .withValueMap(expressionAttributeValues)
                    .withConditionExpression("attribute_exists(PK)")
                    .also {
                        table.updateItem(it)
                    }
        } catch (e: ConditionalCheckFailedException) {
            throw ConditionalCheckFailedException("Cannot Rename node since $nodeID does not exist")
        }
    }

    fun checkIfNodeExistsForWorkspace(nodeID: String, workspaceID: String) : Boolean {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)

        val nodeList: List<Node> = DynamoDBQueryExpression<Node>()
                .withKeyConditionExpression("PK = :pk and SK = :sk")
                .withProjectionExpression("PK")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(Node::class.java, it)
                }

        return nodeList.isNotEmpty()
    }


    fun getAllNodeIDToNodeNameMap(workspaceID: String, itemStatus: ItemStatus) : Map<String, String>{
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Node.name.uppercase())
        expressionAttributeValues[":itemStatus"] = AttributeValue(itemStatus.name)


        return DynamoDBQueryExpression<Node>()
                .withKeyConditionExpression("PK = :workspaceIdentifier and begins_with(SK, :SK)")
                .withFilterExpression("itemStatus = :itemStatus")
                .withProjectionExpression("PK, SK, title")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(Node::class.java, it, dynamoDBMapperConfig)
                }.associate {
                    it.id to it.title
                }
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeRepository::class.java)
    }

}

// TODO(separate out table in code cleanup)

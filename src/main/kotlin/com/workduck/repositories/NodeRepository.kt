package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest
import com.amazonaws.services.dynamodbv2.model.Update
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.utils.Constants
import com.serverless.utils.Constants.getCurrentTime
import com.workduck.models.AccessType
import com.workduck.models.AdvancedElement
import com.workduck.models.Element
import com.workduck.models.IdentifierType
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.Node
import com.workduck.models.NodeAccess
import com.workduck.models.NodeVersion
import com.workduck.utils.AccessItemHelper.getAccessItemPK
import com.workduck.utils.DDBHelper
import com.workduck.utils.DDBTransactionHelper
import org.apache.logging.log4j.LogManager
import com.workduck.utils.Helper
import java.time.Instant

class NodeRepository(
    private val mapper: DynamoDBMapper,
    private val dynamoDB: DynamoDB,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig,
    private val client: AmazonDynamoDB,
    private var tableName: String
)  {

    fun append(nodeID: String, workspaceID: String, userID: String, elements: List<AdvancedElement>, orderList: MutableList<String>): Map<String, Any>? {
        val table = dynamoDB.getTable(tableName)

        /* this is to ensure correct ordering of blocks/ elements */
        var updateExpression = "set dataOrder = list_append(if_not_exists(dataOrder, :empty_list), :orderList), lastEditedBy = :userID, updatedAt = :updatedAt"

        val objectMapper = ObjectMapper()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()

        /* we build updateExpression to enable appending of multiple key value pairs to the map with just one query */
        for ((counter, e) in elements.withIndex()) {
            val entry: String = objectMapper.writeValueAsString(e)
            updateExpression += ", nodeData.${e.id} = :val$counter"
            expressionAttributeValues[":val$counter"] = entry
        }

        expressionAttributeValues[":userID"] = userID
        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":orderList"] = orderList
        expressionAttributeValues[":empty_list"] = mutableListOf<Element>()

        return UpdateItemSpec().withPrimaryKey("PK", workspaceID, "SK", nodeID)
            .withUpdateExpression(updateExpression)
            .withValueMap(expressionAttributeValues)
            .withConditionExpression("attribute_exists(PK) and attribute_exists(SK)")
            .let {
                table.updateItem(it)
                mapOf("nodeID" to nodeID, "appendedElements" to elements)
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
        if(mapOfNodeIDToName.isEmpty()) return mutableListOf()

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


    fun getBlock(nodeID: String, blockID: String, workspaceID: String) : Node? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)

        return  DynamoDBQueryExpression<Node>()
            .withKeyConditionExpression("PK = :pk and SK = :sk")
            .withProjectionExpression("nodeData.$blockID, dataOrder")
            .withExpressionAttributeValues(expressionAttributeValues).let {
                mapper.query(Node::class.java, it)
            }.firstOrNull()

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
                    .withConditionExpression("attribute_exists(PK) and attribute_exists(SK)")
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
                .withKeyConditionExpression("PK = :PK and begins_with(SK, :SK)")
                .withFilterExpression("itemStatus = :itemStatus")
                .withProjectionExpression("PK, SK, title")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(Node::class.java, it, dynamoDBMapperConfig)
                }.associate {
                    it.id to it.title
                }
    }

    fun createBatchNodeAccessItem(nodeAccessItems : List<NodeAccess>){
        val failedBatches = mapper.batchWrite(nodeAccessItems, emptyList<Any>(), dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }

    fun deleteBatchNodeAccessItem(nodeAccessItems : List<NodeAccess>){
        val failedBatches = mapper.batchWrite(emptyList<Any>(), nodeAccessItems, dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }


    fun checkIfAccessRecordExists(nodeID: String, userID: String) : Boolean{
        return mapper.load(NodeAccess::class.java, getAccessItemPK(nodeID), userID) != null
    }


    fun getUserIDsWithNodeAccess(nodeID: String, accessTypeList : List<AccessType>) : List<String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(getAccessItemPK(nodeID))
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.NodeAccess.name)

        return DynamoDBQueryExpression<NodeAccess>()
                .withKeyConditionExpression("PK = :PK")
                .withFilterExpression("itemType = :itemType")
                .withProjectionExpression("SK, accessType")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig)
                }.filter {
                    it.accessType in  accessTypeList}.map { accessItem -> accessItem.userID  }

    }

    fun getSharedUserInformation(nodeID: String) : Map<String, String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(getAccessItemPK(nodeID))
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.NodeAccess.name)

        return DynamoDBQueryExpression<NodeAccess>()
                .withKeyConditionExpression("PK = :PK")
                .withFilterExpression("itemType = :itemType")
                .withProjectionExpression("SK, accessType")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig).associate { accessItem ->
                        accessItem.userID to accessItem.accessType.name
                    }
                }
    }


    fun getNodeByNodeID(nodeID: String) : Node {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":itemStatus"] = AttributeValue(ItemStatus.ACTIVE.name)

        return DynamoDBQueryExpression<Node>()
                .withKeyConditionExpression("SK = :SK  and begins_with(PK, :PK)")
                .withIndexName("SK-PK-Index").withConsistentRead(false)
                .withFilterExpression("itemStatus = :itemStatus")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(Node::class.java, it, dynamoDBMapperConfig).let { nodeList ->
                        nodeList.firstOrNull() ?: throw NoSuchElementException("Requested Resource Not Found")
                    }
                }
    }

    fun getNodeWorkspaceID(nodeID: String) : String{
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":itemStatus"] = AttributeValue(ItemStatus.ACTIVE.name)

        return DynamoDBQueryExpression<Node>()
                .withKeyConditionExpression("SK = :SK  and begins_with(PK, :PK)")
                .withIndexName("SK-PK-Index").withConsistentRead(false)
                .withFilterExpression("itemStatus = :itemStatus")
                .withProjectionExpression("PK")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(Node::class.java, it, dynamoDBMapperConfig).let { nodeList ->
                        nodeList.firstOrNull()?.workspaceIdentifier?.id ?: throw NoSuchElementException("Requested Resource Not Found")
                    }
                }

    }

    fun getTags(nodeID: String, workspaceID: String) : MutableList<String>? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)


        return DynamoDBQueryExpression<Node>()
                .withKeyConditionExpression("PK = :PK and SK=:SK")
                .withProjectionExpression("tags")
                .withExpressionAttributeValues(expressionAttributeValues).let { query ->
                    mapper.query(Node::class.java, query, dynamoDBMapperConfig).getOrNull(0)?.tags
                    }
    }


    companion object {
        private val LOG = LogManager.getLogger(NodeRepository::class.java)
    }

}

// TODO(separate out table in code cleanup)

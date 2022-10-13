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
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec
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
import com.serverless.utils.Messages
import com.workduck.models.AccessType
import com.workduck.models.AdvancedElement
import com.workduck.models.Element
import com.workduck.models.IdentifierType
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.Node
import com.workduck.models.NodeAccess
import com.workduck.models.NodeVersion
import com.workduck.utils.AccessItemHelper
import com.workduck.utils.DDBTransactionHelper
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager
import java.time.Instant


class NodeRepository(
    private val mapper: DynamoDBMapper,
    private val dynamoDB: DynamoDB,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig,
    private val client: AmazonDynamoDB,
    private var tableName: String
) {

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

        return UpdateItemSpec().update(
            pk = workspaceID, sk = nodeID, updateExpression = updateExpression, expressionAttributeValues = expressionAttributeValues,
            conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"
        ).let {
            table.updateItem(it)
            mapOf("nodeID" to nodeID, "appendedElements" to elements)
        }
    }

    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): List<String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Node.name.uppercase())
        expressionAttributeValues[":AK"] = AttributeValue(namespaceID)

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", projectionExpression = "SK",
            filterExpression = "AK = :AK", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).map { node ->
                node.id
            }
        }
    }

    fun getAllNodesWithNamespaceIDAndAccess(namespaceID: String, workspaceID: String, publicAccess: Int): List<String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Node.name.uppercase())
        expressionAttributeValues[":AK"] = AttributeValue(namespaceID)
        expressionAttributeValues[":publicAccess"] = AttributeValue().withN(publicAccess.toString())

        return DynamoDBQueryExpression<Node>().query(
                keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", projectionExpression = "SK",
                filterExpression = "AK = :AK and publicAccess = :publicAccess", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).map { node ->
                node.id
            }
        }
    }

    fun getAllNodesWithNamespaceIDAndStatus(namespaceID: String, workspaceID: String, itemStatus: ItemStatus): List<String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Node.name.uppercase())
        expressionAttributeValues[":AK"] = AttributeValue(namespaceID)
        expressionAttributeValues[":itemStatus"] = AttributeValue(itemStatus.name)

        return DynamoDBQueryExpression<Node>().query(
                keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", projectionExpression = "SK",
                filterExpression = "AK = :AK and itemStatus = :itemStatus", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).map { node ->
                node.id
            }
        }
    }

    fun getAllNodesWithWorkspaceID(workspaceID: String): MutableList<String> {
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":PK"] = workspaceID
        expressionAttributeValues[":SK"] = ItemType.Node.name.uppercase()

        val items: ItemCollection<QueryOutcome?>? = QuerySpec().withKeyConditionExpression("PK = :PK and begins_with(SK, :SK)")
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

        return DynamoDBQueryExpression<Node>().queryWithIndex(
            index = "createdBy-itemType-index", keyConditionExpression = "createdBy = :createdBy  and itemType = :itemType",
            projectionExpression = "PK", expressionAttributeValues = expressionAttributeValues
        ).let { it ->
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).map { node ->
                node.id
            }
        }
    }

    fun createMultipleNodes(listOfNodes: List<Node>) {
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
            LOG.debug("Version mismatch!!")
            null
        } catch (e: java.lang.Exception) {
            LOG.error(e)
            null
        }
    }

    fun updateNodeBlock(nodeID: String, workspaceID: String, updatedBlock: String, blockID: String, userID: String): AdvancedElement? {
        val table = dynamoDB.getTable(tableName)
        val objectMapper = ObjectMapper()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedBlock"] = updatedBlock
        expressionAttributeValues[":userID"] = userID

        return UpdateItemSpec().update(
            pk = workspaceID, sk = nodeID, updateExpression = "SET nodeData.$blockID = :updatedBlock, lastEditedBy = :userID ",
            expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"
        ).let {
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

        val q = DynamoDBQueryExpression<NodeVersion>().query(
            keyConditionExpression = "PK = :pk", filterExpression = "versionStatus = :status and itemType = :NodeVersion",
            expressionAttributeValues = expressionAttributeValues, projectionExpression = "SK"
        )

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

        UpdateItemSpec().update(
            pk = "$nodeID${Constants.DELIMITER}VERSION", sk = oldestUpdatedAt, updateExpression = "SET timeToLive = :ttl, versionStatus = :status",
            expressionAttributeValues = expressionAttributeValues
        ).also {
            table.updateItem(it)
        }
    }

    fun unarchiveAndRenameNodes(mapOfNodeIDToName: Map<String, String>, workspaceID: String): MutableList<String> {
        if (mapOfNodeIDToName.isEmpty()) return mutableListOf()

        val table: Table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any?> = HashMap()
        expressionAttributeValues[":active"] = ItemStatus.ACTIVE.name
        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":expireAt"] = null

        val nodesProcessedList: MutableList<String> = mutableListOf()

        for ((nodeID, nodeName) in mapOfNodeIDToName) {
            try {
                expressionAttributeValues[":title"] = "$nodeName(1)"
                UpdateItemSpec().updateWithNullAttributes(
                    pk = workspaceID, sk = nodeID, updateExpression = "SET itemStatus = :active, title = :title, updatedAt = :updatedAt, expireAt = :expireAt",
                    expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK)"
                ).also {
                    table.updateItem(it)
                    nodesProcessedList += nodeID
                }
            } catch (e: ConditionalCheckFailedException) {
                LOG.warn("nodeID : $nodeID not present in the DB")
            }
        }
        return nodesProcessedList
    }

    fun getBlock(nodeID: String, blockID: String, workspaceID: String): Node? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :pk and SK = :sk", projectionExpression = "nodeData.$blockID, dataOrder",
            expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig)
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

    fun deleteBlockAndDataOrderFromNode(blockIdList: List<String>, workspaceID: String, nodeID: String, userID: String, dataOrder: MutableList<String>) {
        val table = dynamoDB.getTable(tableName)
        val nodeKey = HashMap<String, AttributeValue>()
        nodeKey["PK"] = AttributeValue(workspaceID)
        nodeKey["SK"] = AttributeValue(nodeID)

        val expressionAttributeValues: MutableMap<String, Any> = mutableMapOf()

        val dataOrderList: MutableList<AttributeValue> = mutableListOf()
        var blockIdExpression = ""
        dataOrder.map {
            for (blockId in blockIdList){
                if(!it.equals(blockId)) dataOrderList.add(AttributeValue().withS(it))
                else blockIdExpression += "nodeData.${blockId} "
            }

        }

        expressionAttributeValues[":updatedDataOrder"] = AttributeValue().withL(dataOrderList)
        expressionAttributeValues[":updatedAt"] = AttributeValue().withN(Constants.getCurrentTime().toString())
        expressionAttributeValues[":lastEditedBy"] = AttributeValue().withS(userID)

        val updateExpression = "remove $blockIdExpression " +
                "set dataOrder = :updatedDataOrder, " +
                "updatedAt = :updatedAt, lastEditedBy = :lastEditedBy"

        try {
            UpdateItemSpec().update(
                pk = workspaceID, sk = nodeID, updateExpression = updateExpression,
                expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"
            ).also {
                table.updateItem(it)
            }
        } catch (e: ConditionalCheckFailedException) {
            throw ConditionalCheckFailedException("Cannot delete block since $nodeID does not exist")
        }
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

    private fun getUpdateToAddBlockToNode(block: AdvancedElement?, workspaceID: String, nodeID: String, currentTime: Long): Update {

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

    fun renameNodeInNamespace(nodeID: String, newName: String, userID: String, workspaceID: String, namespaceID: String?) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any?> = HashMap()
        expressionAttributeValues[":title"] = newName
        expressionAttributeValues[":lastEditedBy"] = userID
        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":AK"] = namespaceID

        try {
            UpdateItemSpec().updateWithNullAttributes(
                pk = workspaceID, sk = nodeID, updateExpression = "SET title = :title, updatedAt = :updatedAt, " +
                    "lastEditedBy = :lastEditedBy, AK = :AK",
                expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"
            ).also {
                table.updateItem(it)
            }
        } catch (e: ConditionalCheckFailedException) {
            throw ConditionalCheckFailedException("Cannot Rename node since $nodeID does not exist")
        }
    }

    fun updateNodeNamespace(nodeID: String, workspaceID: String, namespaceID: String) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":AK"] = namespaceID

        UpdateItemSpec().update(
                pk = workspaceID, sk = nodeID, updateExpression = "SET AK = :AK, updatedAt = :updatedAt",
                expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"
        ).also {
            table.updateItem(it)
        }
    }

    fun checkIfNodeExistsForWorkspace(nodeID: String, workspaceID: String): Boolean {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :pk and SK = :sk", projectionExpression = "PK",
            expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig)
        }.isNotEmpty()
    }

    fun getNodeWorkspaceAndNamespace(nodeID: String): Pair<String, String>? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)

        return DynamoDBQueryExpression<Node>().queryWithIndex( index = "SK-PK-Index",
                keyConditionExpression = "SK = :sk and begins_with(PK, :pK)", projectionExpression = "PK, AK",
                expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig)
        }.firstOrNull()?.let { node ->
            Pair(node.workspaceIdentifier.id, node.namespaceIdentifier.id)
        }
    }






    fun getAllNodeIDToNodeNameMap(workspaceID: String, itemStatus: ItemStatus): Map<String, String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Node.name.uppercase())
        expressionAttributeValues[":itemStatus"] = AttributeValue(itemStatus.name)

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", filterExpression = "itemStatus = :itemStatus",
            projectionExpression = "PK, SK, title", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig)
        }.associate { it.id to it.title }
    }

    fun createBatchNodeAccessItem(nodeAccessItems: List<NodeAccess>) {
        val failedBatches = mapper.batchWrite(nodeAccessItems, emptyList<Any>(), dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }

    fun deleteBatchNodeAccessItem(nodeAccessItems: List<NodeAccess>) {
        val failedBatches = mapper.batchWrite(emptyList<Any>(), nodeAccessItems, dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }

    fun deleteBatchNodes(nodes: List<Node>) {
        val failedBatches = mapper.batchWrite(emptyList<Any>(), nodes, dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }

    fun checkIfAccessRecordExists(nodeID: String, userID: String): Boolean {
        return mapper.load(NodeAccess::class.java, AccessItemHelper.getNodeAccessItemPK(nodeID), userID, dynamoDBMapperConfig) != null
    }

    fun checkIfUserHasAccess(nodeID: String, userID: String, accessTypeList: List<AccessType>): Boolean {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(AccessItemHelper.getNodeAccessItemPK(nodeID))
        expressionAttributeValues[":SK"] = AttributeValue(userID)
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.NodeAccess.name)

        return DynamoDBQueryExpression<NodeAccess>().query(
            keyConditionExpression = "PK = :PK", filterExpression = "itemType = :itemType",
            projectionExpression = "SK, accessType", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig)
        }.filter { it.accessType in accessTypeList }.map { accessItem -> accessItem.userID }.isNotEmpty()
    }

    fun getUserNodeAccessRecord(nodeID: String, userID: String) : String {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(AccessItemHelper.getNodeAccessItemPK(nodeID))
        expressionAttributeValues[":SK"] = AttributeValue(userID)
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.NodeAccess.name)

        return DynamoDBQueryExpression<NodeAccess>().query(keyConditionExpression = "PK = :PK and SK = :SK", filterExpression = "itemType = :itemType",
                projectionExpression = "SK, accessType", expressionAttributeValues = expressionAttributeValues).let {
            mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig)
        }.firstOrNull()?.accessType?.name ?: AccessType.NO_ACCESS.name
    }



    fun getSharedUserInformation(nodeID: String) : Map<String, String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(AccessItemHelper.getNodeAccessItemPK(nodeID))
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.NodeAccess.name)

        return DynamoDBQueryExpression<NodeAccess>().query(
            keyConditionExpression = "PK = :PK", filterExpression = "itemType = :itemType",
            projectionExpression = "SK, accessType", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig).associate { accessItem ->
                accessItem.userID to accessItem.accessType.name
            }
        }
    }

    fun getAllSharedNodesWithUser(userID: String): Map<String, NodeAccess> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(userID)
        expressionAttributeValues[":PK"] = AttributeValue(IdentifierType.NODE_ACCESS.name)
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.NodeAccess.name)

        return DynamoDBQueryExpression<NodeAccess>().queryWithIndex(
            index = "SK-PK-Index", keyConditionExpression = "SK = :SK  and begins_with(PK, :PK)",
            filterExpression = "itemType = :itemType", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig).associateBy { nodeAccess -> nodeAccess.node.id }
        }
    }

    fun batchGetNodes(nodeIDList : List<String>, workspaceID: String) : List<Node> {
        if(nodeIDList.isEmpty()) return mutableListOf()
        val keysAndAttributes = TableKeysAndAttributes(tableName)

        for(nodeID in nodeIDList){
            keysAndAttributes.addHashAndRangePrimaryKey("PK", workspaceID, "SK", nodeID)
        }

        val spec = BatchGetItemSpec().withTableKeyAndAttributes(keysAndAttributes)
        val itemOutcome = dynamoDB.batchGetItem(spec)
        val listOfNodesInMapFormat =  itemOutcome.batchGetItemResult.responses[tableName]

        val listOfNodes = mutableListOf<Node>()

        listOfNodesInMapFormat?.map {
            listOfNodes.add(Helper.objectMapper.convertValue(Helper.mapToJson(it), Node::class.java))
        }
        return listOfNodes
    }

    fun batchGetNodeMetadataAndTitle(setOfNodeIDWorkspaceID: Set<Pair<String, String>>) : MutableList<MutableMap<String, AttributeValue>>{
        if(setOfNodeIDWorkspaceID.isEmpty()) return mutableListOf()
        val keysAndAttributes = TableKeysAndAttributes(tableName)
        for (nodeToWorkspacePair in setOfNodeIDWorkspaceID) {
            keysAndAttributes.addHashAndRangePrimaryKey("PK", nodeToWorkspacePair.second, "SK", nodeToWorkspacePair.first)
        }

        keysAndAttributes.withProjectionExpression("PK, SK, title, itemStatus, metadata, createdAt, updatedAt")
        val spec = BatchGetItemSpec().withTableKeyAndAttributes(keysAndAttributes)
        val itemOutcome = dynamoDB.batchGetItem(spec)

        return itemOutcome.batchGetItemResult.responses[tableName]!!

    }

    fun getNodeDataOrderByNodeID(nodeID: String, workspaceID: String): MutableList<String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":itemStatus"] = AttributeValue(ItemStatus.ACTIVE.name)

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "SK = :SK  and  PK = :PK",
            filterExpression = "itemStatus = :itemStatus", projectionExpression = "dataOrder", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).let { nodeList ->
                nodeList.firstOrNull()?.let { node -> node.dataOrder ?: mutableListOf<String>() } ?: throw NoSuchElementException("Requested Resource Not Found")
            }
        }
    }


    fun getNodeByNodeID(nodeID: String, itemStatus: ItemStatus): Node? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":itemStatus"] = AttributeValue(itemStatus.name)

        return DynamoDBQueryExpression<Node>().queryWithIndex(
            index = "SK-PK-Index", keyConditionExpression = "SK = :SK  and begins_with(PK, :PK)",
            filterExpression = "itemStatus = :itemStatus", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).firstOrNull()
        }
    }

    fun getNodeWorkspaceIDAndOwner(nodeID: String): Map<String, String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":itemStatus"] = AttributeValue(ItemStatus.ACTIVE.name)

        val workspaceDetailsMap = mutableMapOf<String, String>()

        return DynamoDBQueryExpression<Node>().queryWithIndex(
            index = "SK-PK-Index", keyConditionExpression = "SK = :SK  and begins_with(PK, :PK)",
            filterExpression = "itemStatus = :itemStatus", projectionExpression = "PK, createdBy", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).let { nodeList ->
                nodeList.firstOrNull()?.let { node ->
                    workspaceDetailsMap[Constants.WORKSPACE_ID] = node.workspaceIdentifier.id
                    workspaceDetailsMap[Constants.WORKSPACE_OWNER] = node.createdBy!!
                    workspaceDetailsMap
                } ?: throw NoSuchElementException("Requested Resource Not Found")
            }
        }
    }

    fun getTags(nodeID: String, workspaceID: String): MutableList<String>? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and SK=:SK", projectionExpression = "tags", expressionAttributeValues = expressionAttributeValues
        ).let { query ->
            mapper.query(Node::class.java, query, dynamoDBMapperConfig).getOrNull(0)?.tags
        }
    }

    fun getMetadataForNodesOfWorkspace(workspaceID: String): Map<String, Map<String, Any?>> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.Node.name)

        return DynamoDBQueryExpression<Node>().queryWithIndex(
            index = "PK-itemType-index", keyConditionExpression = "PK = :PK  and itemType = :itemType",
            expressionAttributeValues = expressionAttributeValues
        ).let { it ->
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).associate { node ->
                node.id to mapOf("metadata" to node.nodeMetaData, "updatedAt" to node.updatedAt, "createdAt" to node.createdAt)
            }
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeRepository::class.java)
    }
}

// TODO(separate out table in code cleanup)

package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec
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
import com.serverless.utils.Constants.NODE_ID_PREFIX
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
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager


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
        expressionAttributeValues[":deleted"] = 1

        return UpdateItemSpec().update(
            pk = workspaceID, sk = nodeID, updateExpression = updateExpression, expressionAttributeValues = expressionAttributeValues,
            conditionExpression = "attribute_exists(PK) and attribute_exists(SK) and deleted <> :deleted"
        ).let {
            table.updateItem(it)
            mapOf("nodeID" to nodeID, "appendedElements" to elements)
        }
    }

    /* when a namespace gets deleted, all the non-deleted nodes of the namespace should get deleted.
       when a user tries to delete a node, only an archived node should be deleted. ( active nodes -> archived -> deleted )
     */
    fun softDeleteNode(nodeID: String, workspaceID: String, userID: String) {
        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":deleted"] = 1
        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":expireAt"] = Helper.getTTLForDeletingNode()
        expressionAttributeValues[":lastEditedBy"] = userID

        val updateExpression = "SET deleted = :deleted, updatedAt = :updatedAt, expireAt = :expireAt, lastEditedBy = " +
                ":lastEditedBy"

        /* the node should not be deleted already ( 0/null != 1 ) */
        val conditionExpression = "deleted <> :deleted and attribute_exists(SK) and attribute_exists(PK)"

        try {
            return UpdateItemSpec().update(
                pk = workspaceID, sk = nodeID, updateExpression = updateExpression,
                expressionAttributeValues = expressionAttributeValues, conditionExpression = conditionExpression
            ).let {
                table.updateItem(it)
            }
        } catch(e: ConditionalCheckFailedException){
            LOG.warn("Failed to delete $nodeID")
            throw IllegalStateException(Messages.ERROR_NAMESPACE_DELETED)
        }


    }

    fun changeNamespace(nodeID: String, workspaceID: String, sourceNamespaceID: String, targetNamespaceID: String, userID: String){
        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":targetNamespace"] = targetNamespaceID
        expressionAttributeValues[":sourceNamespace"] = sourceNamespaceID
        expressionAttributeValues[":lastEditedBy"] = userID
        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":deleted"] = 1 /* no need to change namespace of a deleted node */

        // TODO ( remove when the feature is stable )
        LOG.info("change namespace from $sourceNamespaceID to $targetNamespaceID")
        val updateExpression = "SET updatedAt = :updatedAt, lastEditedBy = :lastEditedBy, AK = :targetNamespace"

        val conditionExpression = "AK = :sourceNamespace and attribute_exists(PK) and attribute_exists(SK) and deleted <> :deleted"

        try {
            return UpdateItemSpec().update(
                pk = workspaceID, sk = nodeID, updateExpression = updateExpression,
                expressionAttributeValues = expressionAttributeValues, conditionExpression = conditionExpression
            ).let {
                table.updateItem(it)
            }
        } catch(e: ConditionalCheckFailedException){
            LOG.warn("Failed to change namespace of $nodeID from $sourceNamespaceID to $targetNamespaceID")
            throw IllegalStateException(Messages.ERROR_NAMESPACE_DELETED)
        }

    }

    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): List<String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Node.name.uppercase())
        expressionAttributeValues[":AK"] = AttributeValue(namespaceID)
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")


        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", projectionExpression = "SK",
            filterExpression = "AK = :AK and deleted <> :deleted", expressionAttributeValues =
            expressionAttributeValues
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
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().query(
                keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", projectionExpression = "SK",
                filterExpression = "AK = :AK and publicAccess = :publicAccess and deleted <> :deleted", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).map { node ->
                node.id
            }
        }
    }


    fun getAllNodesWithWorkspaceID(workspaceID: String): List<String> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Node.name.uppercase())
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", projectionExpression = "SK",
            filterExpression = "deleted <> :deleted", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).map { node ->
                node.id
            }
        }

    }

    fun getAllNodesWithUserID(userID: String): List<String> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":createdBy"] = AttributeValue(userID)
        expressionAttributeValues[":itemType"] = AttributeValue("Node")
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().queryWithIndex(
            index = "createdBy-itemType-index", keyConditionExpression = "createdBy = :createdBy  and itemType = :itemType",
            projectionExpression = "PK", expressionAttributeValues = expressionAttributeValues, filterExpression = "deleted <> :deleted"
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

    fun getNodeBlock(nodeID: String, workspaceID: String, blockID: String) : AdvancedElement {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", projectionExpression = "nodeData.$blockID",
            filterExpression = "deleted <> :deleted", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).first().data?.first() ?: throw NoSuchElementException(Messages.INVALID_BLOCK_ID)
        }

    }

    fun updateNodeBlock(nodeID: String, workspaceID: String, updatedBlock: String, blockID: String, userID: String) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedBlock"] = updatedBlock
        expressionAttributeValues[":userID"] = userID
        expressionAttributeValues[":deleted"] = 1


        UpdateItemSpec().update(
            pk = workspaceID, sk = nodeID, updateExpression = "SET nodeData.$blockID = :updatedBlock, lastEditedBy = :userID ",
            expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK) and deleted <> :deleted"
        ).let {
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
        expressionAttributeValues[":deleted"] = 1

        val nodesProcessedList: MutableList<String> = mutableListOf()

        for ((nodeID, nodeName) in mapOfNodeIDToName) {
            try {
                expressionAttributeValues[":title"] = "$nodeName(1)"
                UpdateItemSpec().updateWithNullAttributes(
                    pk = workspaceID, sk = nodeID, updateExpression = "SET itemStatus = :active, title = :title, updatedAt = :updatedAt, expireAt = :expireAt",
                    expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK) and deleted <> :deleted"
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

    fun getNodeWithBlockAndDataOrder(nodeID: String, blockID: String, workspaceID: String): Node? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")


        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :pk and SK = :sk", projectionExpression = "nodeData.$blockID, dataOrder",
            expressionAttributeValues = expressionAttributeValues, filterExpression = "deleted <> :deleted"
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig)
        }.firstOrNull()
    }

    fun moveBlock(block: AdvancedElement, workspaceIDOfSourceNode: String, sourceNodeID: String, workspaceIDOfDestinationNode: String, destinationNodeID: String, dataOrderSourceNode: MutableList<String>) {

        val currentTime = getCurrentTime()

        dataOrderSourceNode.remove(block.id)

        val deleteBlock = getUpdateToDeleteBlockFromNode(block, workspaceIDOfSourceNode, sourceNodeID, dataOrderSourceNode, currentTime)
        val addBlock = getUpdateToAddBlockToNode(block, workspaceIDOfDestinationNode, destinationNodeID, currentTime)

        val actions: Collection<TransactWriteItem> = listOf(
            TransactWriteItem().withUpdate(deleteBlock),
            TransactWriteItem().withUpdate(addBlock)
        )

        val moveBlockTransaction = TransactWriteItemsRequest().withTransactItems(actions)

        client.transactWriteItems(moveBlockTransaction)
    }

    fun deleteBlockAndDataOrderFromNode(blockIdList: List<String>, workspaceID: String, nodeID: String, userID: String, existingDataOrder: MutableList<String>) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = mutableMapOf()

        var blockIdExpression = ""

        blockIdList.map { blockIDToDelete ->
            blockIdExpression += "nodeData.${blockIDToDelete} ,"
        }

        existingDataOrder.removeAll(blockIdList)
        blockIdExpression = blockIdExpression.dropLast(1)

        expressionAttributeValues[":updatedDataOrder"] = existingDataOrder
        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":lastEditedBy"] = userID
        expressionAttributeValues[":deleted"] = 1

        val updateExpression = "remove $blockIdExpression " +
                "set dataOrder = :updatedDataOrder, " +
                "updatedAt = :updatedAt, lastEditedBy = :lastEditedBy"

        try {
            UpdateItemSpec().update(
                pk = workspaceID, sk = nodeID, updateExpression = updateExpression,
                expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK) and deleted <> :deleted"
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
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        val updateExpression1 = "remove nodeData.${block?.id} " +
            "set dataOrder = :dataOrderNode1, " +
            "updatedAt = :updatedAt"

        return Update().withTableName(tableName)
            .withKey(nodeKey)
            .withUpdateExpression(updateExpression1)
            .withExpressionAttributeValues(expressionAttributeValues)
            .withConditionExpression("deleted <> :deleted")
    }

    private fun getUpdateToAddBlockToNode(block: AdvancedElement?, workspaceID: String, nodeID: String, currentTime: Long): Update {

        val nodeKey = HashMap<String, AttributeValue>()
        nodeKey["PK"] = AttributeValue(workspaceID)
        nodeKey["SK"] = AttributeValue(nodeID)

        val expressionAttributeValues: MutableMap<String, AttributeValue> = mutableMapOf()
        expressionAttributeValues[":updatedAt"] = AttributeValue().withN(currentTime.toString())
        expressionAttributeValues[":orderList"] = AttributeValue().withL(AttributeValue().withS(block?.id))
        expressionAttributeValues[":block"] = AttributeValue(Helper.objectMapper.writeValueAsString(block))
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        val updateExpression = "set dataOrder = list_append(dataOrder, :orderList), " +
            "nodeData.${block?.id} = :block, updatedAt = :updatedAt"

        return Update().withTableName(tableName)
            .withKey(nodeKey)
            .withUpdateExpression(updateExpression)
            .withExpressionAttributeValues(expressionAttributeValues)
            .withConditionExpression("deleted <> :deleted")
    }

    fun updateNodeNamespaceAndPublicAccess(nodeID: String, workspaceID: String, namespaceID: String, publicAccess: Int?) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()

        val updateExpression = when(publicAccess != null){
            true -> {
                expressionAttributeValues[":publicAccess"] = publicAccess
                "SET updatedAt = :updatedAt, lastEditedBy = :lastEditedBy, AK = :AK, publicAccess = :publicAccess"
            }
            false -> {
                "SET updatedAt = :updatedAt, lastEditedBy = :lastEditedBy, AK = :AK"
            }
        }

        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":AK"] = namespaceID
        expressionAttributeValues[":deleted"] = 1


        UpdateItemSpec().update(
            pk = workspaceID, sk = nodeID, updateExpression = updateExpression,
            expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK) and deleted <> :deleted"
        ).also {
            table.updateItem(it)
        }
    }

    fun renameNodeInNamespaceWithAccessValue(nodeID: String, newName: String, userID: String, workspaceID: String, namespaceID: String, publicAccess: Int?) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()

        val updateExpression = when(publicAccess != null){
            true -> {
                expressionAttributeValues[":publicAccess"] = publicAccess
                "SET title = :title, updatedAt = :updatedAt, lastEditedBy = :lastEditedBy, AK = :AK, publicAccess = :publicAccess"
            }
            false -> {
                "SET title = :title, updatedAt = :updatedAt, lastEditedBy = :lastEditedBy, AK = :AK"
            }
        }

        expressionAttributeValues[":title"] = newName
        expressionAttributeValues[":lastEditedBy"] = userID
        expressionAttributeValues[":updatedAt"] = getCurrentTime()
        expressionAttributeValues[":AK"] = namespaceID
        expressionAttributeValues[":deleted"] = 1


        try {
            UpdateItemSpec().update(
                pk = workspaceID, sk = nodeID, updateExpression = updateExpression,
                expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK) and deleted <> :deleted"
            ).also {
                table.updateItem(it)
            }
        } catch (e: ConditionalCheckFailedException) {
            throw ConditionalCheckFailedException("Cannot Rename node since $nodeID does not exist")
        }
    }

    fun checkIfNodeExistsForWorkspace(nodeID: String, workspaceID: String): Boolean {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")


        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :pk and SK = :sk", projectionExpression = "PK",
            expressionAttributeValues = expressionAttributeValues, filterExpression = "deleted <> :deleted"
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig)
        }.isNotEmpty()
    }

    fun getNodeWorkspaceAndNamespace(nodeID: String): Pair<String, String>? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue().withS(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":SK"] = AttributeValue().withS(nodeID)
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().queryWithIndex( index = "SK-PK-Index",
                keyConditionExpression = "SK = :SK and begins_with(PK, :PK)", projectionExpression = "PK, AK",
                expressionAttributeValues = expressionAttributeValues, filterExpression = "deleted <> :deleted"
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
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", filterExpression = "itemStatus = :itemStatus and deleted <> :deleted",
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

    fun deleteNodeAccessItem(userID: String, nodeID: String){
        val table = dynamoDB.getTable(tableName)
        DeleteItemSpec().withPrimaryKey("PK", AccessItemHelper.getNodeAccessItemPK(nodeID), "SK", userID)
            .also { table.deleteItem(it) }
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
            keyConditionExpression = "PK = :PK and SK = :SK", filterExpression = "itemType = :itemType",
            projectionExpression = "SK, accessType", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig)
        }.filter { it.accessType in accessTypeList }.map { accessItem -> accessItem.userID }.isNotEmpty()
    }


    fun getNodeAccessItem(nodeID: String, userID: String, accessTypeList: List<AccessType>): NodeAccess? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(AccessItemHelper.getNamespaceAccessItemPK(nodeID))
        expressionAttributeValues[":SK"] = AttributeValue(userID)
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.NodeAccess.name)

        return DynamoDBQueryExpression<NodeAccess>().query(
                keyConditionExpression = "PK = :PK and SK = :SK", filterExpression = "itemType = :itemType",
                projectionExpression = "SK, accessType, workspaceID", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig).firstOrNull()?.takeIf { item ->
                item.accessType in accessTypeList
            }
        }
    }

    fun getUserNodeAccessType(nodeID: String, userID: String) : AccessType {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(AccessItemHelper.getNodeAccessItemPK(nodeID))
        expressionAttributeValues[":SK"] = AttributeValue(userID)
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.NodeAccess.name)

        return DynamoDBQueryExpression<NodeAccess>().query(keyConditionExpression = "PK = :PK and SK = :SK", filterExpression = "itemType = :itemType",
                projectionExpression = "SK, accessType", expressionAttributeValues = expressionAttributeValues).let {
            mapper.query(NodeAccess::class.java, it, dynamoDBMapperConfig)
        }.firstOrNull()?.accessType ?: AccessType.NO_ACCESS
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

        keysAndAttributes.withProjectionExpression("PK, SK, title, itemStatus, metadata, createdAt, updatedAt, createdBy")
        val spec = BatchGetItemSpec().withTableKeyAndAttributes(keysAndAttributes)
        val itemOutcome = dynamoDB.batchGetItem(spec)

        return itemOutcome.batchGetItemResult.responses[tableName]!!

    }

    fun getNodeDataOrderByNodeID(nodeID: String, workspaceID: String): MutableList<String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":itemStatus"] = AttributeValue(ItemStatus.ACTIVE.name)
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "SK = :SK  and  PK = :PK",
            filterExpression = "itemStatus = :itemStatus and deleted <> :deleted", projectionExpression = "dataOrder", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).let { nodeList ->
                nodeList.firstOrNull()?.let { node -> node.dataOrder ?: mutableListOf<String>() } ?: throw NoSuchElementException("Requested Resource Not Found")
            }
        }
    }


    fun getNodeByNodeID(nodeID: String, itemStatus: ItemStatus? = null): Node? {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        /* if itemStatus not provided, search for both types of nodes */
        val filterExpression = when(itemStatus == null){
            true -> {
                expressionAttributeValues[":active"] = AttributeValue(ItemStatus.ACTIVE.name)
                expressionAttributeValues[":archived"] = AttributeValue(ItemStatus.ARCHIVED.name)
                "(itemStatus = :active or itemStatus = :archived) and  deleted <> :deleted"
            }
            false -> {
                expressionAttributeValues[":itemStatus"] = AttributeValue(itemStatus.name)
                "itemStatus = :itemStatus and deleted <> :deleted"
            }
        }

        return DynamoDBQueryExpression<Node>().queryWithIndex(
            index = "SK-PK-Index", keyConditionExpression = "SK = :SK  and begins_with(PK, :PK)",
            filterExpression = filterExpression, expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).firstOrNull()
        }
    }

    fun getNodeWorkspaceIDAndOwner(nodeID: String): Map<String, String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":itemStatus"] = AttributeValue(ItemStatus.ACTIVE.name)
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        val workspaceDetailsMap = mutableMapOf<String, String>()

        return DynamoDBQueryExpression<Node>().queryWithIndex(
            index = "SK-PK-Index", keyConditionExpression = "SK = :SK  and begins_with(PK, :PK)",
            filterExpression = "itemStatus = :itemStatus and deleted <> :deleted", projectionExpression = "PK, createdBy", expressionAttributeValues = expressionAttributeValues
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
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and SK=:SK", projectionExpression = "tags", expressionAttributeValues = expressionAttributeValues,
            filterExpression = "deleted <> :deleted"
        ).let { query ->
            mapper.query(Node::class.java, query, dynamoDBMapperConfig).getOrNull(0)?.tags
        }
    }

    fun getMetadataForNodesOfWorkspace(workspaceID: String): Map<String, Map<String, Any?>> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue().withS(NODE_ID_PREFIX)
        expressionAttributeValues[":itemType"] = AttributeValue(ItemType.Node.name)
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().query(
            keyConditionExpression = "PK = :PK and begins_with(SK, :SK)", expressionAttributeValues = expressionAttributeValues,
            filterExpression = "itemType = :itemType and deleted <> :deleted", projectionExpression = "metadata, createdAt, updatedAt"
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).associate { node ->
                node.id to mapOf("metadata" to node.metadata, "updatedAt" to node.updatedAt, "createdAt" to node.createdAt)
            }
        }
    }


    fun getOwnerDetailsFromNodeID(nodeID: String) : Map<String, String> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(nodeID)
        expressionAttributeValues[":PK"] = AttributeValue(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":deleted"] = AttributeValue().withN("1")

        return DynamoDBQueryExpression<Node>().queryWithIndex(
            index = "SK-PK-Index", keyConditionExpression = "SK = :SK  and begins_with(PK, :PK)",
            projectionExpression = "PK, SK, createdBy", expressionAttributeValues = expressionAttributeValues,
            filterExpression = "deleted <> :deleted"
        ).let {
            mapper.query(Node::class.java, it, dynamoDBMapperConfig).firstOrNull()?.let { node ->
                mapOf((node.createdBy ?: "" ) to AccessType.OWNER.name)
            } ?: throw IllegalArgumentException(Messages.INVALID_NAMESPACE_ID)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeRepository::class.java)
    }
}

// TODO(separate out table in code cleanup)

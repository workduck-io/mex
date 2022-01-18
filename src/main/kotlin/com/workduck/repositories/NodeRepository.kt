package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.*
import com.workduck.utils.DDBHelper
import com.workduck.utils.DDBTransactionHelper
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager
import java.time.Instant
import java.util.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties


class NodeRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB
) : Repository<Node> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    private val objectMapper = Helper.objectMapper

    override fun get(identifier: Identifier): Entity? =
            mapper.load(Node::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)


//    private fun orderBlocks(node: Node): Entity =
//        node.apply {
//            node.data?.let { data ->
//                (node.dataOrder?.mapNotNull { blockId ->
//                    data.find { element -> blockId == element.id }
//                } ?: emptyList())
//                        .also {
//                            node.data = it.toMutableList()
//                        }
//            }
//        }


//    private fun orderBlocks(node: Node): Entity =
//        node.apply {
//            node.data?.let { data ->
//                (node.dataOrder?.mapNotNull { blockId ->
//                    data.find { element -> blockId == element.id }
//                } ?: emptyList())
//                        .also {
//                            node.data = it.toMutableList()
//                        }
//            }
//        }


    fun append(nodeID: String, userID: String, elements: List<AdvancedElement>, orderList: List<String>): Map<String, Any>? {
        val table = dynamoDB.getTable(tableName)

        /* this is to ensure correct ordering of blocks/ elements */
        var updateExpression = "set dataOrder = list_append(if_not_exists(dataOrder, :empty_list), :orderList), lastEditedBy = :userID, updatedAt = :updatedAt"


        val expressionAttributeValues: MutableMap<String, Any> = HashMap()

        /* we build updateExpression to enable appending of multiple key value pairs to the map with just one query */
        for ((counter, e) in elements.withIndex()) {
            val entry: String = objectMapper.writeValueAsString(e)
            updateExpression += ", nodeData.${e.id} = :val$counter"
            expressionAttributeValues[":val$counter"] = entry
        }

        expressionAttributeValues[":userID"] = userID
        expressionAttributeValues[":updatedAt"] = System.currentTimeMillis()
        expressionAttributeValues[":orderList"] = orderList
        expressionAttributeValues[":empty_list"] = mutableListOf<Element>()


        return UpdateItemSpec().withPrimaryKey("PK", nodeID, "SK", nodeID)
            .withUpdateExpression(updateExpression)
            .withValueMap(expressionAttributeValues)
            .withConditionExpression("attribute_exists(PK) and attribute_exists(SK)")
            .let{
                table.updateItem(it)
                mapOf("nodeID" to nodeID, "appendedElements" to elements)
            }
    }

    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): MutableList<String>? {

        val akValue = "$workspaceID#$namespaceID"
        return try {
            DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(akValue, "itemType-AK-index", dynamoDB, "Node")
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    fun getAllNodesWithWorkspaceID(workspaceID: String): MutableList<String>? {

        return try {
            return DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(workspaceID, "itemType-AK-index", dynamoDB, "Node")
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    override fun delete(identifier: Identifier): Identifier? {
        val table = dynamoDB.getTable(tableName)

        DeleteItemSpec()
            .withPrimaryKey("PK", identifier.id, "SK", identifier.id)
            .also { table.deleteItem(it) }

        return identifier
    }

    override fun create(t: Node): Node {
        TODO("Not yet implemented")
    }

    fun createNodeVersion(node : Node, nodeVersion: NodeVersion){
        try {

            val nodeToSave = Node()
            nodeToSave.id = node.id
            nodeToSave.idCopy = node.idCopy
            nodeToSave.publicAccess = node.publicAccess
            nodeToSave.createdAt = node.createdAt
            nodeToSave.updatedAt = node.updatedAt
            nodeToSave.nodeVersionCount = node.nodeVersionCount + 1
            nodeToSave.lastVersionCreatedAt = nodeVersion.createdAt
            nodeToSave.version = node.version /* this version is to avoid conflicts during concurrent updates and auto-incrementing. In this flow we don't really need this to be updated. */

            /* REFER NOTE HERE : https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.OptimisticLocking.html */


            val dynamoDBMapperUpdateConfig = DynamoDBMapperConfig.Builder()
                    .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                    .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
                    .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                    .withTypeConverterFactory(DynamoDBTypeConverterFactory.standard())
                    .build()

            val transactionWriteRequest = TransactionWriteRequest()
            transactionWriteRequest.addUpdate(nodeToSave)
            transactionWriteRequest.addPut(nodeVersion)

            LOG.info("Saving nodeVersion : $nodeVersion")
            DDBTransactionHelper(mapper).transactionWrite(transactionWriteRequest, dynamoDBMapperUpdateConfig, client)


            //mapper.save(nodeVersion)
        }
        catch(e : Exception){
            LOG.error(e)
        }
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

    override fun update(t: Node): Node? {
        TODO("Not yet implemented")
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

    fun updateNodeBlock(nodeID: String, updatedBlock: String, blockID: String, userID: String): AdvancedElement? {
        val table = dynamoDB.getTable(tableName)
        val objectMapper = ObjectMapper()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedBlock"] = updatedBlock
        expressionAttributeValues[":userID"] = userID

        return UpdateItemSpec().withPrimaryKey("PK", nodeID, "SK", nodeID)
            .withUpdateExpression("SET nodeData.$blockID = :updatedBlock, lastEditedBy = :userID ")
            .withValueMap(expressionAttributeValues)
            .withConditionExpression("attribute_exists(PK) and attribute_exists(SK)")
            .let {
                table.updateItem(it)
                objectMapper.readValue(updatedBlock)
            }
    }

    fun getMetaDataForActiveVersions(nodeID : String) : MutableList<String?> {
        LOG.info("Inside getAllVersionsOfNode function")


        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS("${nodeID}#VERSION")
        expressionAttributeValues[":status"] = AttributeValue().withS("ACTIVE")
        expressionAttributeValues[":NodeVersion"] = AttributeValue().withS("Node Version")


        val nodeVersionList: List<NodeVersion> = DynamoDBQueryExpression<NodeVersion>()
                .withKeyConditionExpression("PK = :pk")
                .withFilterExpression("versionStatus = :status and itemType = :NodeVersion")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withProjectionExpression("SK").let{
                    mapper.query(NodeVersion::class.java, it, dynamoDBMapperConfig)
                }


        val itemList: MutableList<String?> = mutableListOf()
        nodeVersionList.map {
           itemList.add(it.sk)
        }
        return itemList

    }

    fun getAllArchivedNodesOfWorkspace(workspaceID : String) : MutableList<String>?{

        try {
            val table: Table = dynamoDB.getTable(tableName)
            val index: Index = table.getIndex("WS-itemStatus-Index")


            val expressionAttributeValues: MutableMap<String, Any> = HashMap()
            expressionAttributeValues[":workspaceID"] = workspaceID
            expressionAttributeValues[":archived"] = "ARCHIVED"
            expressionAttributeValues[":node"] = "Node"

            val querySpec = QuerySpec()
                    .withKeyConditionExpression("workspaceIdentifier = :workspaceID and itemStatus = :archived")
                    .withFilterExpression("itemType = :node")
                    .withValueMap(expressionAttributeValues)
                    .withProjectionExpression("PK")


            val items: ItemCollection<QueryOutcome?>? = index.query(querySpec)
            val iterator: Iterator<Item> = items!!.iterator()

            var nodeIDList: MutableList<String> = mutableListOf()
            while (iterator.hasNext()) {
                val item: Item = iterator.next()
                nodeIDList = (nodeIDList + (item["PK"] as String)).toMutableList()
            }
            return nodeIDList
        }
        catch( e: Exception){
            println(e)
            return null
        }

    }


    fun setTTLForOldestVersion(nodeID : String, oldestUpdatedAt : String){

        val table : Table = dynamoDB.getTable(tableName)

        val now: Long = Instant.now().epochSecond // unix time
        val ttl = (60).toLong()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":ttl"] = (now + ttl)
        expressionAttributeValues[":status"] = "INACTIVE"
        expressionAttributeValues[":updatedAt"] = System.currentTimeMillis()


        val u = UpdateItemSpec().withPrimaryKey("PK", "$nodeID#VERSION", "SK", oldestUpdatedAt)
                .withUpdateExpression("SET timeToLive = :ttl, versionStatus = :status, updatedAt = :updatedAt ")
                .withValueMap(expressionAttributeValues)

        try {
            table.updateItem(u)
        } catch (e: Exception) {
            println(e)
        }


    }

    fun unarchiveOrArchiveNodes(nodeIDList: List<String>, status : String) : MutableList<String> {
        val table: Table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":active"] = status

        val nodesProcessedList : MutableList<String> = mutableListOf()
        for(nodeID in nodeIDList){
            try {
                UpdateItemSpec().withPrimaryKey("PK", nodeID, "SK", nodeID)
                        .withUpdateExpression("SET itemStatus = :active")
                        .withValueMap(expressionAttributeValues)
                        .withConditionExpression("attribute_exists(PK)")
                        .also {
                            table.updateItem(it)
                            nodesProcessedList += nodeID
                        }
            }
            catch(e: ConditionalCheckFailedException){
                LOG.warn("nodeID : $nodeID not present in the DB")
            }
        }

        return nodesProcessedList
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeRepository::class.java)
    }



    fun toggleNodePublicAccess(nodeID: String, accessValue: Long) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":true"] = accessValue

        UpdateItemSpec().withPrimaryKey("PK", nodeID, "SK", nodeID)
                .withUpdateExpression("SET publicAccess = :true")
                .withValueMap(expressionAttributeValues).also{
                    table.updateItem(it)
                }
    }

    fun getPublicNode(nodeID: String) : Node? {
        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(nodeID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(nodeID)
        expressionAttributeValues[":true"] = AttributeValue().withN("1")


        val queryExpression = DynamoDBQueryExpression<Node>()
                .withKeyConditionExpression("PK = :pk and SK = :sk")
                .withFilterExpression("publicAccess = :true")
                .withExpressionAttributeValues(expressionAttributeValues)


        val nodeList: List<Node> = mapper.query(Node::class.java, queryExpression, dynamoDBMapperConfig)

        return if(nodeList.isNotEmpty()) nodeList[0]
        else null

    }
}

//TODO(separate out table in code cleanup)

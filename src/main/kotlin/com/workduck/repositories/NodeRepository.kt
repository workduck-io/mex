package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Node
import com.workduck.models.Identifier
import com.workduck.models.Entity
import com.workduck.models.AdvancedElement
import com.workduck.models.Element
import com.workduck.models.NodeVersion
import com.workduck.utils.DDBHelper
import com.workduck.utils.DDBTransactionHelper
import java.time.Instant

import org.apache.logging.log4j.LogManager

class NodeRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig
) : Repository<Node> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    override fun get(identifier: Identifier): Entity? {
        return try {
            val node = mapper.load(Node::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
            orderBlocks(node)
            return node
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    private fun orderBlocks(node: Node): Entity =
        node.apply {
            node.data?.let { data ->
                (node.dataOrder?.mapNotNull { blockId ->
                    data.find { element -> blockId == element.id }
                } ?: emptyList())
                        .also {
                            node.data = it.toMutableList()
                        }
            }
        }


    fun append(nodeID: String, userID: String, elements: List<AdvancedElement>, orderList: MutableList<String>): Map<String, Any>? {
        val table = dynamoDB.getTable(tableName)

        /* this is to ensure correct ordering of blocks/ elements */
        var updateExpression = "set nodeDataOrder = list_append(if_not_exists(nodeDataOrder, :empty_list), :orderList), lastEditedBy = :userID"

        val objectMapper = ObjectMapper()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()

        /* we build updateExpression to enable appending of multiple key value pairs to the map with just one query */
        for ((counter, e) in elements.withIndex()) {
            val entry: String = objectMapper.writeValueAsString(e)
            updateExpression += ", nodeData.${e.id} = :val$counter"
            expressionAttributeValues[":val$counter"] = entry
        }

        expressionAttributeValues[":userID"] = userID
        expressionAttributeValues[":orderList"] = orderList
        expressionAttributeValues[":empty_list"] = mutableListOf<Element>()

        val updateItemSpec: UpdateItemSpec = UpdateItemSpec().withPrimaryKey("PK", nodeID, "SK", nodeID)
            .withUpdateExpression(updateExpression)
            .withValueMap(expressionAttributeValues)

        return try {
            table.updateItem(updateItemSpec)
            mapOf("nodeID" to nodeID, "appendedElements" to elements)
        } catch (e: Exception) {
            LOG.info(e)
            null
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

        val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
            .withPrimaryKey("PK", identifier.id, "SK", identifier.id)

        return try {
            table.deleteItem(deleteItemSpec)
            identifier
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    override fun create(t: Node): Node {
        TODO("Not yet implemented")
    }


    fun createNode(node: Node, nodeVersion: NodeVersion): Node? {
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


    fun updateNode(node: Node, nodeVersion: NodeVersion): Node? {
        val dynamoDBMapperUpdateConfig = DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build()


        return try {
            val transactionWriteRequest = TransactionWriteRequest()
            transactionWriteRequest.addUpdate(node)
            transactionWriteRequest.addPut(nodeVersion)
            DDBTransactionHelper(mapper).transactionWrite(transactionWriteRequest, dynamoDBMapperUpdateConfig)
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

        val u = UpdateItemSpec().withPrimaryKey("PK", nodeID, "SK", nodeID)
            .withUpdateExpression("SET nodeData.$blockID = :updatedBlock, lastEditedBy = :userID ")
            .withValueMap(expressionAttributeValues)

        return try {
            table.updateItem(u)
            objectMapper.readValue(updatedBlock)
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeRepository::class.java)
    }

    fun getMetaDataForActiveVersions(nodeID : String) : MutableList<String>? {
        val table = dynamoDB.getTable(tableName)
        println("Inside getAllVersionsOfNode function")

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":pk"] = "${nodeID}#VERSION"
        expressionAttributeValues[":status"] = "ACTIVE"

        val querySpec = QuerySpec()
                .withKeyConditionExpression("PK = :pk")
                .withFilterExpression("versionStatus = :status")
                .withValueMap(expressionAttributeValues)
                .withProjectionExpression("SK")


        return try {
            val items: ItemCollection<QueryOutcome?>? = table.query(querySpec)
            val iterator: Iterator<Item> = items!!.iterator()

            var itemList: MutableList<String> = mutableListOf()
            while (iterator.hasNext()) {
                val item: Item = iterator.next()
                itemList = (itemList + (item["SK"] as String)).toMutableList()
            }
            itemList
        } catch (e : Exception){
            println(e)
            null
        }

    }

    fun setTTLForOldestVersion(nodeID : String, oldestUpdatedAt : String){

        val table = dynamoDB.getTable("local-mex-history")

        val now: Long = Instant.now().epochSecond // unix time
        val ttl = (60).toLong()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":ttl"] = (now + ttl)
        expressionAttributeValues[":status"] = "INACTIVE"


        val u = UpdateItemSpec().withPrimaryKey("PK", "$nodeID#VERSION", "SK", oldestUpdatedAt)
                .withUpdateExpression("SET timeToLive = :ttl, versionStatus = :status ")
                .withValueMap(expressionAttributeValues)

        try {
            table.updateItem(u)
        } catch (e: Exception) {
            println(e)
        }


    }



}

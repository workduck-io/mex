package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Node
import com.workduck.models.Identifier
import com.workduck.models.Entity
import com.workduck.models.AdvancedElement
import com.workduck.models.Element
import com.workduck.utils.DDBHelper
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


    fun append(nodeID: String, userID: String, elements: MutableList<AdvancedElement>, orderList: MutableList<String>): Map<String, Any>? {
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

    override fun update(t: Node): Node? {
        val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build()

        return try {
            mapper.save(t, dynamoDBMapperConfig)
            t
        } catch (e: ConditionalCheckFailedException) {
            /* Will happen only in race condition because we're making the versions same in the service */
            /* What should be the flow from here on? Call NodeService().update()? */
            println("Version mismatch!!")
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
}

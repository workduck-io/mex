package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Index
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.workduck.models.Identifier
import com.workduck.models.Page
import org.apache.logging.log4j.LogManager

class PageRepository <T : Page> (
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB,
        private val tableName: String
) : Repository<T>{

    override fun create(t: T): T {
        TODO("Not yet implemented")
    }

    override fun update(t: T): T? {
        TODO("Not yet implemented")
    }


    override fun get(identifier: Identifier, clazz: Class<T>): T? {
        TODO("Not yet implemented")
    }


    override fun delete(identifier: Identifier): Identifier {
        val table = dynamoDB.getTable(tableName)
        DeleteItemSpec().withPrimaryKey("PK", identifier.id, "SK", identifier.id).also { table.deleteItem(it) }
        return identifier
    }


    fun togglePagePublicAccess(snippetID: String, accessValue: Long) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":publicAccess"] = accessValue

        UpdateItemSpec().withPrimaryKey("PK", snippetID, "SK", snippetID)
                .withUpdateExpression("SET publicAccess = :publicAccess")
                .withValueMap(expressionAttributeValues).let{
                    table.updateItem(it)
                }
    }

    fun getPublicPage(pageID: String, clazz: Class<T>): T {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(pageID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(pageID)
        expressionAttributeValues[":true"] = AttributeValue().withN("1")

        val queryExpression = DynamoDBQueryExpression<T>()
                .withKeyConditionExpression("PK = :pk and SK = :sk")
                .withFilterExpression("publicAccess = :true")
                .withExpressionAttributeValues(expressionAttributeValues)

        val pageList: List<T> = mapper.query(clazz, queryExpression, dynamoDBMapperConfig)

        return if (pageList.isNotEmpty()) pageList[0]
        else throw IllegalArgumentException("Given ID is not public")
    }



    fun unarchiveOrArchivePages(pageIDList: List<String>, status: String): MutableList<String> {
        val table: Table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":active"] = status

        val pagesProcessedList: MutableList<String> = mutableListOf()
        for (pageID in pageIDList) {
            try {
                UpdateItemSpec().withPrimaryKey("PK", pageID, "SK", pageID)
                        .withUpdateExpression("SET itemStatus = :active")
                        .withValueMap(expressionAttributeValues)
                        .withConditionExpression("attribute_exists(PK)")
                        .also {
                            table.updateItem(it)
                            pagesProcessedList += pageID
                        }
            } catch (e: ConditionalCheckFailedException) {
                LOG.warn("pageID : $pageID not present in the DB")
            }
        }

        return pagesProcessedList
    }


    fun getAllArchivedPagesOfWorkspace(workspaceID: String, itemType: String): MutableList<String> {

        val table: Table = dynamoDB.getTable(tableName)
        val index: Index = table.getIndex("WS-itemStatus-Index")

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":workspaceID"] = workspaceID
        expressionAttributeValues[":archived"] = "ARCHIVED"
        expressionAttributeValues[":node"] = itemType

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


    companion object {
        private val LOG = LogManager.getLogger(PageRepository::class.java)
    }

}
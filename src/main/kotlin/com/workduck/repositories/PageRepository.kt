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
import com.serverless.utils.Constants
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.Node
import com.workduck.models.Page
import com.workduck.models.Relationship
import com.workduck.models.Snippet
import com.workduck.models.WorkspaceIdentifier
import com.workduck.utils.SnippetHelper
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

    override fun update(t: T): T {
        TODO("Not yet implemented")
    }

//    override fun getWithException(pkIdentifier: Identifier, skIdentifier: Identifier, clazz: Class<T>): T {
//        return get( pkIdentifier, skIdentifier, clazz) ?: throw NOTfound
//    }

    fun T?.withNotFoundException():T = this ?: throw Exception("aaa")

    override fun get(pkIdentifier: Identifier, skIdentifier: Identifier, clazz: Class<T>): T? {
        return mapper.load(clazz, pkIdentifier, skIdentifier.id, dynamoDBMapperConfig)
    }


    override fun delete(pkIdentifier: Identifier, skIdentifier: Identifier): Identifier {
        TODO("Using deleteComment instead")
    }


    fun togglePagePublicAccess(sk: String, workspaceID: String, accessValue: Long) {
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":publicAccess"] = accessValue

        UpdateItemSpec().update(pk = workspaceID, sk = sk, updateExpression = "SET publicAccess = :publicAccess",
                expressionAttributeValues = expressionAttributeValues).let{
            table.updateItem(it)
        }
    }

    fun getPublicPage(sk: String, clazz: Class<T>): T {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(sk)
        expressionAttributeValues[":PK"] = AttributeValue("WORKSPACE")
        expressionAttributeValues[":true"] = AttributeValue().withN("1")
        expressionAttributeValues[":itemStatus"] = AttributeValue(ItemStatus.ACTIVE.name)

        return DynamoDBQueryExpression<T>().queryWithIndex(index = "SK-PK-Index", keyConditionExpression = "SK = :SK  and begins_with(PK, :PK)",
                filterExpression = "publicAccess = :true and itemStatus = :itemStatus", expressionAttributeValues = expressionAttributeValues).let {
            mapper.query(clazz, it, dynamoDBMapperConfig).let { list ->
                if(list.isNotEmpty()) list[0]
                else throw NoSuchElementException("Requested Resource Not Found")
            }
        }
    }

    fun unarchiveOrArchivePages(pageIDList: List<String>, workspaceID: String, itemStatus: ItemStatus): MutableList<String> {
        val table: Table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":active"] = itemStatus.name
        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()

        val pagesProcessedList: MutableList<String> = mutableListOf()
        for (pageID in pageIDList) {
            try {
                UpdateItemSpec().update(pk = workspaceID, sk = pageID, updateExpression = "SET itemStatus = :active, updatedAt = :updatedAt",
                        conditionExpression = "attribute_exists(PK)", expressionAttributeValues = expressionAttributeValues).also {
                    table.updateItem(it)
                    pagesProcessedList += pageID
                }
            } catch (e: ConditionalCheckFailedException) {
                LOG.warn("Conditional check failed for $pageID : ${e.message}")
            }
        }

        return pagesProcessedList
    }


    fun getAllArchivedPagesOfWorkspace(workspaceID: String, itemType: ItemType): MutableList<String> {

        val table: Table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":workspaceID"] = workspaceID
        expressionAttributeValues[":archived"] = ItemStatus.ARCHIVED.name
        expressionAttributeValues[":itemType"] = itemType.name.uppercase()

        val querySpec = QuerySpec()
                .withKeyConditionExpression("PK = :workspaceID and begins_with(SK, :itemType)")
                .withFilterExpression("itemStatus = :archived")
                .withValueMap(expressionAttributeValues)
                .withProjectionExpression("SK")

        val items: ItemCollection<QueryOutcome?>? = table.query(querySpec)
        val iterator: Iterator<Item> = items!!.iterator()

        var pageIDList: MutableList<String> = mutableListOf()
        while (iterator.hasNext()) {
            val item: Item = iterator.next()
            pageIDList = (pageIDList + (item["SK"] as String)).toMutableList()
        }
        return pageIDList

    }


    companion object {
        private val LOG = LogManager.getLogger(PageRepository::class.java)
    }

}
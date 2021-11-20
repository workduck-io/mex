package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.UserIdentifierRecord

class UserIdentifierMappingRepository(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig
) : Repository<UserIdentifierRecord> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    val table: Table = dynamoDB.getTable(tableName)

    override fun create(t: UserIdentifierRecord): UserIdentifierRecord {
        TODO("Not yet implemented")
    }

    override fun update(t: UserIdentifierRecord): UserIdentifierRecord {
        TODO("Not yet implemented")
    }

    override fun get(identifier: Identifier): Entity {
        TODO("Not yet implemented")
    }

    override fun delete(identifier: Identifier): Identifier? {
        TODO("Not yet implemented")
    }

    fun getRecordsByUserID(userID: String): MutableList<String> {
        val querySpec = QuerySpec()

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":userID"] = userID

        querySpec.withKeyConditionExpression("PK = :userID")
            .withValueMap(expressionAttributeValues)

        val items: ItemCollection<QueryOutcome?>? = table.query(querySpec)
        val iterator: Iterator<Item> = items!!.iterator()

        val listOfJSON: MutableList<String> = mutableListOf()
        while (iterator.hasNext()) {
            val item: Item = iterator.next()
            listOfJSON += item.toJSON()
        }
        return listOfJSON
    }

    fun deleteUserIdentifierMapping(userID: String, identifier: Identifier): Map<String, String>? {
        val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
            .withPrimaryKey("PK", userID, "SK", identifier.id)

        return try {
            table.deleteItem(deleteItemSpec)
            mapOf("userID" to userID, "identifierID" to identifier.id)
        } catch (e: Exception) {
            null
        }
    }

    fun createBookmark(userID: String, nodeID: String) : String?{

        try {
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()
            expressionAttributeValues[":map"] = mutableMapOf(nodeID to nodeID)


            val updateItemSpec: UpdateItemSpec = UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID#BOOKMARK", "SK", "$userID#BOOKMARK")
                    .withUpdateExpression("set bookmarkedNodes = :map")
                    .withConditionExpression("attribute_not_exists(bookmarkedNodes)")
                    .withValueMap(expressionAttributeValues)

            table.updateItem(updateItemSpec)

            return nodeID

        }
        catch (e : ConditionalCheckFailedException){
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()
            expressionAttributeValues[":nodeID"] = nodeID


            val updateExpression = "set bookmarkedNodes.${nodeID} = :nodeID"

            val updateItemSpec: UpdateItemSpec = UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID#BOOKMARK", "SK", "$userID#BOOKMARK")
                    .withUpdateExpression(updateExpression)
                    .withValueMap(expressionAttributeValues)

            table.updateItem(updateItemSpec)
            return nodeID
        }
        catch (e : Exception){
            println(e)
            return null
        }

    }


    fun deleteBookmark(userID: String, nodeID: String) : String? {

        return try {
            val updateItemSpec: UpdateItemSpec = UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID#BOOKMARK", "SK", "$userID#BOOKMARK")
                    .withUpdateExpression("remove bookmarkedNodes.${nodeID}")

            table.updateItem(updateItemSpec)
            nodeID
        }
        catch(e :  Exception){
            println(e)
            null
        }

    }

    fun isNodeBookmarkedForUser(nodeID: String, userID: String) : Boolean? {

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":pk"] = "$userID#BOOKMARK"
        expressionAttributeValues[":sk"] = "$userID#BOOKMARK"


        val querySpec: QuerySpec = QuerySpec()
                .withKeyConditionExpression("PK = :pk and SK = :sk")
                .withValueMap(expressionAttributeValues)
                .withFilterExpression("attribute_exists(bookmarkedNodes.$nodeID)")
                .withProjectionExpression("bookmarkedNodes.$nodeID")

        val items: ItemCollection<QueryOutcome?>? = table.query(querySpec)

        return items?.let {
            val iterator: Iterator<Item> = it.iterator()
            iterator.hasNext()
        }

    }

    fun getAllBookmarkedNodesByUser(userID: String) : MutableList<String>? {

        try {
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()
            expressionAttributeValues[":pk"] = "$userID#BOOKMARK"
            expressionAttributeValues[":sk"] = "$userID#BOOKMARK"

            val nodeID = "NODE11"

            val querySpec: QuerySpec = QuerySpec()
                    .withKeyConditionExpression("PK = :pk and SK = :sk")
                    .withValueMap(expressionAttributeValues)
                    .withFilterExpression("attribute_exists(bookmarkedNodes.$nodeID)")
                    .withProjectionExpression("bookmarkedNodes.$nodeID")



            val items: ItemCollection<QueryOutcome?>? = table.query(querySpec)


            val itemList: MutableList<String> = mutableListOf()
            if (items != null) {
                println(items)
                val iterator: Iterator<Item> = items.iterator()

                while (iterator.hasNext()) {
                    val item: Item = iterator.next()
                    println(item)
                    (item["bookmarkedNodes"] as Map<String, String>).forEach {
                        itemList += it.value
                    }
                }
            }
            return itemList
            //println("List of bookmarked nodes : $itemList")
        }
        catch(e : Exception){
            println(e)
            return null
        }
    }


    fun createBookmarksInBatch(userID: String, nodeIDList: List<String>) : List<String>?{

        try {
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()
            val nodeIDMap = mutableMapOf<String, String>()

            for(nodeID in nodeIDList){
                nodeIDMap[nodeID] = nodeID
            }

            expressionAttributeValues[":map"] = nodeIDMap

            val updateItemSpec: UpdateItemSpec = UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID#BOOKMARK", "SK", "$userID#BOOKMARK")
                    .withUpdateExpression("set bookmarkedNodes = :map")
                    .withConditionExpression("attribute_not_exists(bookmarkedNodes)")
                    .withValueMap(expressionAttributeValues)

            table.updateItem(updateItemSpec)

            return nodeIDList

        }
        catch (e : ConditionalCheckFailedException){
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()


            var updateExpression : String = "set"
            for((counter,nodeID) in nodeIDList.withIndex()){
                updateExpression += " bookmarkedNodes.$nodeID = :val$counter,"
                expressionAttributeValues[":val$counter"] = nodeID
            }

            updateExpression = updateExpression.dropLast(1)

            val updateItemSpec: UpdateItemSpec = UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID#BOOKMARK", "SK", "$userID#BOOKMARK")
                    .withUpdateExpression(updateExpression)
                    .withValueMap(expressionAttributeValues)

            table.updateItem(updateItemSpec)
            return nodeIDList
        }
        catch (e : Exception){
            println(e)
            return null
        }

    }

    fun deleteBookmarksInBatch(userID: String, nodeIDList: List<String>) : List<String>?{
        return try {
            var updateExpression : String = "remove"

            for(nodeID in nodeIDList){
                updateExpression += " bookmarkedNodes.$nodeID,"
            }
            updateExpression = updateExpression.dropLast(1)
            val updateItemSpec: UpdateItemSpec = UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID#BOOKMARK", "SK", "$userID#BOOKMARK")
                    .withUpdateExpression(updateExpression)

            table.updateItem(updateItemSpec)
            return nodeIDList
        }
        catch(e :  Exception){
            println(e)
            null
        }

    }
}

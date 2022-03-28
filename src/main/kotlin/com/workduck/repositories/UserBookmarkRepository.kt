package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.serverless.utils.Constants
import org.apache.logging.log4j.LogManager

class UserBookmarkRepository(
        private val table : Table
)  {


    fun createBookmark(userID: String, nodeID: String) {

        try {
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()
            expressionAttributeValues[":map"] = mutableMapOf(nodeID to nodeID)


            return UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID${Constants.DELIMITER}BOOKMARK", "SK", "$userID${Constants.DELIMITER}BOOKMARK")
                    .withUpdateExpression("set bookmarkedNodes = :map")
                    .withConditionExpression("attribute_not_exists(bookmarkedNodes)")
                    .withValueMap(expressionAttributeValues)
                    .let{
                        table.updateItem(it)
                    }
        }
        catch (e : ConditionalCheckFailedException){
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()
            expressionAttributeValues[":nodeID"] = nodeID

            val updateExpression = "set bookmarkedNodes.${nodeID} = :nodeID"

            return UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID${Constants.DELIMITER}BOOKMARK", "SK", "$userID${Constants.DELIMITER}BOOKMARK")
                    .withUpdateExpression(updateExpression)
                    .withValueMap(expressionAttributeValues)
                    .let{
                        table.updateItem(it)
                    }
        }
    }


    fun deleteBookmark(userID: String, nodeID: String) {

        return UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID${Constants.DELIMITER}BOOKMARK", "SK", "$userID${Constants.DELIMITER}BOOKMARK")
                    .withUpdateExpression("remove bookmarkedNodes.${nodeID}")
                    .let {
                        table.updateItem(it)
                    }
    }

    fun isNodeBookmarkedForUser(nodeID: String, userID: String) : Boolean? {

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":pk"] = "$userID${Constants.DELIMITER}BOOKMARK"
        expressionAttributeValues[":sk"] = "$userID${Constants.DELIMITER}BOOKMARK"


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

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":pk"] = "$userID${Constants.DELIMITER}BOOKMARK"
        expressionAttributeValues[":sk"] = "$userID${Constants.DELIMITER}BOOKMARK"

        val items: ItemCollection<QueryOutcome?>? = QuerySpec()
                .withKeyConditionExpression("PK = :pk and SK = :sk")
                .withValueMap(expressionAttributeValues)
                .let { table.query(it) }

        val itemList: MutableList<String> = mutableListOf()



        items?.map {
            val iterator: Iterator<Item> = items.iterator()
            while (iterator.hasNext()) {
                val item: Item = iterator.next()

                (item["bookmarkedNodes"] as Map<String, String>).forEach {
                    itemList += it.value
                }
            }

        }

        return itemList
    }




    fun createBookmarksInBatch(userID: String, nodeIDList: List<String>) {

        try {
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()
            val nodeIDMap = mutableMapOf<String, String>()

            for(nodeID in nodeIDList){
                nodeIDMap[nodeID] = nodeID
            }

            expressionAttributeValues[":map"] = nodeIDMap

            UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID${Constants.DELIMITER}BOOKMARK", "SK", "$userID${Constants.DELIMITER}BOOKMARK")
                    .withUpdateExpression("set bookmarkedNodes = :map")
                    .withConditionExpression("attribute_not_exists(bookmarkedNodes)")
                    .withValueMap(expressionAttributeValues)
                    .let{
                        table.updateItem(it)
                    }

        }
        catch (e : ConditionalCheckFailedException){
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()


            var updateExpression : String = "set"
            for((counter,nodeID) in nodeIDList.withIndex()){
                updateExpression += " bookmarkedNodes.$nodeID = :val$counter,"
                expressionAttributeValues[":val$counter"] = nodeID
            }

            updateExpression = updateExpression.dropLast(1)

            UpdateItemSpec()
                    .withPrimaryKey("PK", "$userID${Constants.DELIMITER}BOOKMARK", "SK", "$userID${Constants.DELIMITER}BOOKMARK")
                    .withUpdateExpression(updateExpression)
                    .withValueMap(expressionAttributeValues)
                    .let {
                        table.updateItem(it)
                    }
        }
    }

    fun deleteBookmarksInBatch(userID: String, nodeIDList: List<String>) {

        var updateExpression : String = "remove"

        for(nodeID in nodeIDList){
            updateExpression += " bookmarkedNodes.$nodeID,"
        }

        /* remove the extra comma */
        updateExpression = updateExpression.dropLast(1)
        UpdateItemSpec()
                .withPrimaryKey("PK", "$userID${Constants.DELIMITER}BOOKMARK", "SK", "$userID${Constants.DELIMITER}BOOKMARK")
                .withUpdateExpression(updateExpression)
                .let{
                    table.updateItem(it)
                }
    }

    companion object {
        private val LOG = LogManager.getLogger(UserBookmarkRepository::class.java)
    }
}

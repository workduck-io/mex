package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.repositories.UserBookmarkRepository
import com.workduck.utils.DDBHelper
import org.apache.logging.log4j.LogManager

class UserBookmarkService {

    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()

    private val userBookmarkRepository: UserBookmarkRepository = UserBookmarkRepository(dynamoDB, mapper, dynamoDBMapperConfig)


    fun createBookmark(userID: String, nodeID: String) : String?{
        return userBookmarkRepository.createBookmark(userID, nodeID)
    }

    fun deleteBookmark(userID: String, nodeID: String) : String?{
        return userBookmarkRepository.deleteBookmark(userID, nodeID)
    }

    fun getAllBookmarkedNodesByUser(userID: String) : MutableList<String>?{
        return userBookmarkRepository.getAllBookmarkedNodesByUser(userID)
    }

    fun isNodeBookmarkedForUser(nodeID: String, userID: String) : Boolean? {
        return userBookmarkRepository.isNodeBookmarkedForUser(nodeID, userID)
    }

    fun createBookmarksInBatch(userID: String, nodeIDList: List<String>) : List<String>?{
        return userBookmarkRepository.createBookmarksInBatch(userID, nodeIDList)
    }

    fun deleteBookmarksInBatch(userID: String, nodeIDList: List<String>) : List<String>?{
        return userBookmarkRepository.deleteBookmarksInBatch(userID, nodeIDList)
    }

    companion object {
        private val LOG = LogManager.getLogger(UserBookmarkService::class.java)
    }
}

fun main() {
    val json: String = """
		{
			"userID" : "USER49",
			"identifier" : "NAMESPACE1"
		}
		"""
    // UserIdentifierMappingService().createUserIdentifierRecord(json)
    //println(UserIdentifierMappingService().getUserRecords("USER49").toString())
    // UserIdentifierMappingService().deleteUserIdentifierMapping("USER49", "NAMESPACE1")
    //UserIdentifierMappingService().createBookmark("USER49", "NODE10")

    //UserIdentifierMappingService().removeBookmark("USER49", "NODE10")
    //UserIdentifierMappingService().getAllBookmarkedNodesByUser("USER49")


    //xval list = mutableListOf("NODE11", "NODE12")
    //UserIdentifierMappingService().createBookmarxksInBatch("USER49", list)
    //UserIdentifierMappingService().deleteBookmarksInBatch("USER49", list)
}

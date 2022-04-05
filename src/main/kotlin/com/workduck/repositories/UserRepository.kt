package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.workduck.models.User
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.utils.DDBHelper
import org.apache.logging.log4j.LogManager

class UserRepository(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig
) : Repository<User> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    override fun get(identifier: Identifier, clazz: Class<User>): User? {
        TODO("Not yet implemented")
    }

    override fun create(t: User): User {
        TODO("Not yet implemented")
    }

    override fun update(t: User): User {
        TODO("Not yet implemented")
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


    fun getAllUsersWithNamespaceID(namespaceID: String): MutableList<String>? {

        return try {
            DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(namespaceID, "itemType-AK-index", dynamoDB, "UserIdentifierRecord")
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    fun getAllUsersWithWorkspaceID(workspaceID: String): MutableList<String>? {

        return try {
            DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(workspaceID, "itemType-AK-index", dynamoDB, "UserIdentifierRecord")
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }


    companion object {
        private val LOG = LogManager.getLogger(UserRepository::class.java)
    }

}

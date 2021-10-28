package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.workduck.models.*

class NamespaceRepository(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Namespace> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    override fun get(identifier: Identifier): Entity? {
        return try {
            return mapper.load(Namespace::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
        } catch (e: Exception) {
            null
        }
    }

    override fun create(t: Namespace): Namespace {
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
            null
        }
    }

    override fun update(t: Namespace): Namespace {
        TODO("Not yet implemented")
    }

    fun getNamespaceData(namespaceIDList: List<String>): MutableMap<String, Namespace?>? {
        val namespaceMap: MutableMap<String, Namespace?> = mutableMapOf()
        return try {
            for (namespaceID in namespaceIDList) {
                val namespace: Namespace? = mapper.load(Namespace::class.java, namespaceID, namespaceID, dynamoDBMapperConfig)
                namespaceMap[namespaceID] = namespace
            }
            namespaceMap
        } catch (e: Exception) {
            null
        }
        TODO("we also need to have some sort of filter which filters out all the non-namespace ids")
        TODO("this code can be reused for similar workspace functionality")
    }
}

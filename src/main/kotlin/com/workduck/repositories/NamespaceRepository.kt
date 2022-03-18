package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Namespace

import org.apache.logging.log4j.LogManager

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
        return mapper.load(Namespace::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
    }

    override fun create(t: Namespace): Namespace {
        TODO("Not yet implemented")
    }

    override fun delete(identifier: Identifier): Identifier? {
        val table = dynamoDB.getTable(tableName)

        val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
            .withPrimaryKey("PK", identifier.id, "SK", identifier.id)


        table.deleteItem(deleteItemSpec)
        return identifier

    }

    override fun update(t: Namespace): Namespace {
        TODO("Not yet implemented")
    }

    fun getNamespaceData(namespaceIDList: List<String>): MutableMap<String, Namespace?>? {
        val namespaceMap: MutableMap<String, Namespace?> = mutableMapOf()

        for (namespaceID in namespaceIDList) {
            val namespace: Namespace? = mapper.load(Namespace::class.java, namespaceID, namespaceID, dynamoDBMapperConfig)
            namespaceMap[namespaceID] = namespace
        }
        return namespaceMap

        TODO("we also need to have some sort of filter which filters out all the non-namespace ids")
        TODO("this code can be reused for similar workspace functionality")
    }


}

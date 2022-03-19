package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Node
import com.workduck.models.Page

class PageRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB,
        private val tableName: String
) : Repository<Page>{

    override fun create(t: Page): Page? {
        TODO("Not yet implemented")
    }

    override fun update(t: Page): Page? {
        TODO("Not yet implemented")
    }

    fun getPage(identifier: Identifier, clazz: Class<Page> ): Entity? =
        mapper.load(clazz, identifier.id, identifier.id, dynamoDBMapperConfig)?.let { node -> orderBlocks(node) }



    override fun delete(identifier: Identifier): Identifier {
        val table = dynamoDB.getTable(tableName)

        DeleteItemSpec().withPrimaryKey("PK", identifier.id, "SK", identifier.id)
                .also { table.deleteItem(it) }

        return identifier
    }


    private fun orderBlocks(page: Page): Entity =
            page.apply {
                page.data?.let { data ->
                    (
                            page.dataOrder?.mapNotNull { blockId ->
                                data.find { element -> blockId == element.id }
                            } ?: emptyList()
                            )
                            .also {
                                page.data = it.toMutableList()
                            }
                }
            }

    override fun get(identifier: Identifier): Entity? {
        TODO("Not yet implemented")
    }

}
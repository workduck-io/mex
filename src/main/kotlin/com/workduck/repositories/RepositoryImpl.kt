package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.workduck.models.Entity
import com.workduck.models.Identifier
import org.apache.logging.log4j.LogManager

class RepositoryImpl<T : Entity>(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val repository: Repository<T>,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig
) : Repository<T> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    override fun get(pkIdentifier: Identifier, skIdentifier: Identifier, clazz: Class<T>): T? {
        return mapper.load(clazz, pkIdentifier.id, skIdentifier.id, dynamoDBMapperConfig)
    }

    override fun delete(pkIdentifier: Identifier, skIdentifier: Identifier): Identifier {
        val table = dynamoDB.getTable(tableName)
        DeleteItemSpec().withPrimaryKey("PK", pkIdentifier.id, "SK", skIdentifier.id).also { table.deleteItem(it) }
        return skIdentifier
    }

    override fun create(t: T){
        mapper.save(t, dynamoDBMapperConfig)
    }

    override fun update(t: T){

        val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build()

        mapper.save(t, dynamoDBMapperConfig)
    }

    companion object {
        private val LOG = LogManager.getLogger(RepositoryImpl::class.java)
    }
}

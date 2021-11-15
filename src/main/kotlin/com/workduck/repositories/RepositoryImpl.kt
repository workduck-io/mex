package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.service.UserService
import org.apache.logging.log4j.LogManager
import java.lang.Exception

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

    override fun get(identifier: Identifier): Entity? {
        return repository.get(identifier)
    }

    override fun delete(identifier: Identifier): Identifier? {
        return repository.delete(identifier)
    }

    override fun create(t: T): T? {
        return try {
            mapper.save(t, dynamoDBMapperConfig)
            t
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    override fun update(t: T): T? {

        val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build()

        return try {
            mapper.save(t, dynamoDBMapperConfig)
            t
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(RepositoryImpl::class.java)
    }
}

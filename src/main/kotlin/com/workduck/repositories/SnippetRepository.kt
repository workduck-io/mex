package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Snippet

class SnippetRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB
): Repository<Snippet> {
    override fun create(t: Snippet): Snippet? {
        TODO("Not yet implemented")
    }

    override fun update(t: Snippet): Snippet? {
        TODO("Not yet implemented")
    }

    override fun get(identifier: Identifier): Entity? {
        TODO("Not yet implemented")
    }

    override fun delete(identifier: Identifier): Identifier? {
        TODO("Not yet implemented")
    }
}
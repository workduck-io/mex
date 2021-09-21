package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AbstractAmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.workduck.models.Entity
import com.workduck.models.Identifier

class RepositoryImpl<T: Entity>(
    val amazonDynamoDB: AmazonDynamoDB
): Repository<T> {
    override fun get(identifier: Identifier): T {
        TODO("Not yet implemented")
    }

    override fun delete(identifier: Identifier) {
        TODO("Not yet implemented")
    }

    override fun create(t: T): T {
        TODO("Not yet implemented")
    }

    override fun update(t: T): T {
        TODO("Not yet implemented")
    }
}
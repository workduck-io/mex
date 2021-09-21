package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.workduck.models.Node
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper

/**
 * contains all node related logic
 */
class NodeService {
    //Todo: Inject them from handlers
    private val client : AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val repository : Repository<Node> = RepositoryImpl(client);
}
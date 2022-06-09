package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.NamespaceRequest
import com.serverless.models.requests.WDRequest

import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Namespace
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.WorkspaceIdentifier

import com.workduck.repositories.NamespaceRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class NamespaceService {

    private val objectMapper = Helper.objectMapper
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

    private val namespaceRepository: NamespaceRepository = NamespaceRepository(dynamoDB, mapper, dynamoDBMapperConfig)
    private val repository: Repository<Namespace> = RepositoryImpl(dynamoDB, mapper, namespaceRepository, dynamoDBMapperConfig)

    fun createNamespace(namespaceRequest: WDRequest?): Entity? {
        val namespace: Namespace = createNamespaceObjectFromNamespaceRequest(namespaceRequest as NamespaceRequest?) ?: return null
        return repository.create(namespace)
    }

    fun getNamespace(namespaceID: String): Entity? {
        return repository.get(NamespaceIdentifier(namespaceID), NamespaceIdentifier(namespaceID), Namespace::class.java)
    }

    fun updateNamespace(namespaceRequest: WDRequest?): Entity? {
        val namespace: Namespace = createNamespaceObjectFromNamespaceRequest(namespaceRequest as NamespaceRequest?) ?: return null
        namespace.createdAt = null
        return repository.update(namespace)
    }

    fun deleteNamespace(namespaceID: String): Identifier? {
        return repository.delete(NamespaceIdentifier(namespaceID), NamespaceIdentifier(namespaceID))
    }

    fun getNamespaceData(namespaceIDList: List<String>): MutableMap<String, Namespace?>? {
        return namespaceRepository.getNamespaceData(namespaceIDList)
    }

    private fun createNamespaceObjectFromNamespaceRequest(namespaceRequest : NamespaceRequest?) : Namespace? {
        return namespaceRequest?.let {
            Namespace(id = it.id,
                    name = it.name,
                    workspaceIdentifier = WorkspaceIdentifier(it.workspaceID))
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(NamespaceService::class.java)
    }
}



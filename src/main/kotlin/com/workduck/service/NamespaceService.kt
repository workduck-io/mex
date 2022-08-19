package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.serverless.models.requests.NamespaceRequest
import com.serverless.models.requests.WDRequest
import com.serverless.transformers.NamespaceTransformer
import com.serverless.transformers.Transformer

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

class NamespaceService (


    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
    private val dynamoDB: DynamoDB = DynamoDB(client),
    private val mapper: DynamoDBMapper = DynamoDBMapper(client),

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    },

    private val dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

    private val namespaceRepository: NamespaceRepository = NamespaceRepository(dynamoDB, mapper, dynamoDBMapperConfig),
    private val repository: Repository<Namespace> = RepositoryImpl(dynamoDB, mapper, namespaceRepository, dynamoDBMapperConfig),

    private val nodeService : NodeService = NodeService()
) {

    fun createNamespace(namespaceRequest: WDRequest, workspaceID: String, createdBy: String): Entity {
        val namespace: Namespace = createNamespaceObjectFromNamespaceRequest(namespaceRequest as NamespaceRequest, workspaceID, createdBy, createdBy)
        require(!checkIfNamespaceNameExists(workspaceID, namespace.name)) { "Cannot use an existing Namespace Name"}
        return repository.create(namespace)
    }

    fun getNamespace(namespaceID: String, workspaceID: String): Entity? {
        return namespaceRepository.get(WorkspaceIdentifier(workspaceID), NamespaceIdentifier(namespaceID), Namespace::class.java)
    }

    fun updateNamespace(namespaceRequest: WDRequest, workspaceID: String, lastEditedBy: String) {
        val namespace: Namespace = createNamespaceObjectFromNamespaceRequest(namespaceRequest as NamespaceRequest, workspaceID, null, lastEditedBy)
        namespace.createdAt = null
        repository.update(namespace)
    }

    fun deleteNamespace(namespaceID: String, workspaceID: String): Identifier {
        return repository.delete(WorkspaceIdentifier(workspaceID), NamespaceIdentifier(namespaceID))
    }


    fun makeNamespacePublic(namespaceID: String, workspaceID: String) {
        require( !namespaceRepository.isNamespacePublic(namespaceID, workspaceID) ) {"Namespace already public"}
        val nodeIDList = nodeService.getAllNodesWithNamespaceIDAndAccess(namespaceID, workspaceID, 0) /* get all private nodes */
        nodeService.makeNodesPublicOrPrivateInParallel(nodeIDList, workspaceID, 1)
        namespaceRepository.setPublicAccessValue(namespaceID, workspaceID, 1)

    }

    fun makeNamespacePrivate(namespaceID: String, workspaceID: String) {
        require( namespaceRepository.isNamespacePublic(namespaceID, workspaceID) ) {"Namespace already private"}
        val nodeIDList = nodeService.getAllNodesWithNamespaceIDAndAccess(namespaceID, workspaceID, 1) /* get all public nodes */
        nodeService.makeNodesPublicOrPrivateInParallel(nodeIDList, workspaceID, 0)
        namespaceRepository.setPublicAccessValue(namespaceID, workspaceID, 1)

    }

    fun getAllNamespaceData(workspaceID: String): List<Namespace> {
        return namespaceRepository.getAllNamespaceData(workspaceID)
    }

    private fun checkIfNamespaceNameExists(workspaceID: String, namespaceName: String) : Boolean {
        return namespaceRepository.checkIfNamespaceNameExists(workspaceID, namespaceName)
    }

    fun getPublicNamespace(namespaceID: String): Namespace {
        return namespaceRepository.getPublicNamespace(namespaceID)
    }


    private fun createNamespaceObjectFromNamespaceRequest(namespaceRequest : NamespaceRequest, workspaceID: String, createdBy: String?, lastEditedBy: String) : Namespace {
        return Namespace(
                    name = namespaceRequest.name,
                    createdBy = createdBy,
                    lastEditedBy = lastEditedBy,
                    workspaceIdentifier = WorkspaceIdentifier(workspaceID))

    }

    companion object {
        private val LOG = LogManager.getLogger(NamespaceService::class.java)
    }
}


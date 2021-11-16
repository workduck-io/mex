package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Namespace
import com.workduck.models.NamespaceIdentifier

import com.workduck.repositories.NamespaceRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import org.apache.logging.log4j.LogManager

class NamespaceService {
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

    fun createNamespace(jsonString: String): Entity? {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val namespace: Namespace = objectMapper.readValue(jsonString)
        LOG.info("Creating namespace : $namespace")
        return repository.create(namespace)
    }

    fun getNamespace(namespaceID: String): Entity? {
        LOG.info("Getting namespace with id : $namespaceID")
        return repository.get(NamespaceIdentifier(namespaceID))
    }

    fun updateNamespace(jsonString: String): Entity? {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val tempNamespace: Namespace = objectMapper.readValue(jsonString)

        val namespace : Namespace = Namespace.createNamespaceWithSkAndCreatedAtNull(tempNamespace)

        LOG.info("Updating namespace : $namespace")
        return repository.update(namespace)
    }

    fun deleteNamespace(namespaceID: String): Identifier? {
        LOG.info("Deleting namespace with id : $namespaceID")
        return repository.delete(NamespaceIdentifier(namespaceID))
    }

    fun getNamespaceData(namespaceIDList: List<String>): MutableMap<String, Namespace?>? {
        LOG.info("Getting namespaces with ids : $namespaceIDList")
        return namespaceRepository.getNamespaceData(namespaceIDList)
    }

    companion object {
        private val LOG = LogManager.getLogger(NamespaceService::class.java)
    }
}

fun main() {

    val json: String = """
		{
			"id": "NAMESPACE1",
            "workspaceIdentifier" : "WORKSPACE1", 
			"name": "Engineering"
		}
		"""

    val jsonUpdate: String = """
		{
			"id" : "NAMESPACE1",
			"name": "Engineering - Team 1"
		
		}
		"""

    // NamespaceService().createNamespace(json)
    println(NamespaceService().getNamespace("NAMESPACE1"))
    // NamespaceService().updateNamespace(jsonUpdate)
    // println(NamespaceService().deleteNamespace("NAMESPACE1"))
}

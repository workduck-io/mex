package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.*
import com.workduck.repositories.NamespaceRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper

class NamespaceService {
	private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
	private val dynamoDB: DynamoDB = DynamoDB(client)
	private val mapper = DynamoDBMapper(client)
	private val tableName: String = System.getenv("TABLE_NAME")

	private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
		.withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
		.build()

	private val namespaceRepository: NamespaceRepository = NamespaceRepository(dynamoDB, mapper, dynamoDBMapperConfig)
	private val repository: Repository<Namespace> = RepositoryImpl(dynamoDB, mapper, namespaceRepository, dynamoDBMapperConfig)



	fun createNamespace(jsonString : String) {
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val namespace: Namespace = objectMapper.readValue(jsonString)

		/* since idCopy is SK for Namespace object, it can't be null if not sent from frontend */
		namespace.idCopy = namespace.id

		repository.create(namespace)
	}

	fun getNamespace(namespaceID : String): String {
		val namespace: Entity = repository.get(NamespaceIdentifier(namespaceID))
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		return objectMapper.writeValueAsString(namespace)
	}


	fun updateNamespace(jsonString: String) {
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val namespace: Namespace = objectMapper.readValue(jsonString)

		/* since idCopy is SK for Namespace object, it can't be null if not sent from frontend */
		namespace.idCopy = namespace.id

		/* to avoid updating createdAt un-necessarily */
		namespace.createdAt = null

		repository.update(namespace)
	}

	fun deleteNamespace(namespaceID : String) {
		repository.delete(NamespaceIdentifier(namespaceID))
	}

	fun getNamespaceData(namespaceIDList : List<String>) : MutableList<String>{
		return namespaceRepository.getNamespaceData(namespaceIDList)
	}

}

fun main() {

	val json : String = """
		{
			"id": "NAMESPACE1",
            "workspaceIdentifier" : "WORKSPACE1", 
			"name": "Engineering"
		}
		"""

	val jsonUpdate : String = """
		{
			"id" : "NAMESPACE1",
			"name": "Engineering - Team 1"
		
		}
		"""

	//NamespaceService().createNamespace(json)
	//println(NamespaceService().getNamespace("NAMESPACE1"))
	//NamespaceService().updateNamespace(jsonUpdate)
	println(NamespaceService().deleteNamespace("NAMESPACE1"))

}
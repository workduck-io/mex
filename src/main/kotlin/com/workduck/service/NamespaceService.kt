package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.models.*
import com.workduck.repositories.NamespaceRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper

class NamespaceService {
	private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
	private val dynamoDB: DynamoDB = DynamoDB(client)
	private val mapper = DynamoDBMapper(client)
	private val namespaceRepository : NamespaceRepository = NamespaceRepository(dynamoDB, mapper)
	private val repository: Repository<Namespace> = RepositoryImpl(dynamoDB, mapper, namespaceRepository)



	fun createNamespace(){
		val ns : Namespace = Namespace(
			id = "NMSPC1",
			name = "Engineering",
			workspaceIdentifier = WorkspaceIdentifier("WS1234"),
			createdAt = System.currentTimeMillis(),
		)
		repository.create(ns)
	}

	fun getNamespace(){
		val namespace : Entity = repository.get(NamespaceIdentifier("NMSPC1"))
		println(namespace)
	}

	/* we'll need createdAt field's value else default value defined in model would be picked up */
	fun updateNamespace(){
		val ns : Namespace = Namespace(
			id = "NMSPC1",
			name = "Engineering 2.0",
			workspaceIdentifier = WorkspaceIdentifier("WS1234")
		)
		repository.update(ns)
	}

	fun deleteNamespace(){
		repository.delete(NamespaceIdentifier("NMSPC1"), "elementsTableTest")

	}

}

fun main(){
	//NamespaceService().createNamespace()
	//NamespaceService().getNamespace()
	//NamespaceService().updateNamespace()
	//NamespaceService().deleteNamespace()

}
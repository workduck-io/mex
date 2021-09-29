package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.models.*
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.UserRepository
import com.workduck.utils.DDBHelper

class UserService {
	private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
	private val dynamoDB: DynamoDB = DynamoDB(client)
	private val mapper = DynamoDBMapper(client)

	private val userRepository : UserRepository = UserRepository(mapper)
	private val repository: Repository<User> = RepositoryImpl(dynamoDB, mapper, userRepository)

	fun createUser(){
		val user : User = User(
			id = "USER49",
			name = "Varun",
			workspaceIdentifiers = mutableListOf(WorkspaceIdentifier("WS1234")),
			namespaceIdentifiers = mutableListOf(NamespaceIdentifier("NAMESPACE1"))
		)
		repository.create(user)

	}

	fun getUser(){
		val user : Entity = repository.get(UserIdentifier("USER49"))
		println(user)
	}

	fun updateUser(){
		val user : User = User(
			id = "USER49",
			name = "Varun Garg",
			workspaceIdentifiers = mutableListOf(WorkspaceIdentifier("WS1234")),
			namespaceIdentifiers = mutableListOf(NamespaceIdentifier("NAMESPACE1"))
		)
		repository.update(user)
	}

	fun deleteUser(){
		repository.delete(UserIdentifier("USER49"), "elementsTableTest")
	}
}


fun main(){
	//UserService().createUser()
	//UserService().getUser()
	//UserService().updateUser()
	UserService().deleteUser()
}
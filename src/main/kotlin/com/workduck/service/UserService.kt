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

	private val userRepository : UserRepository = UserRepository(dynamoDB, mapper)
	private val repository: Repository<User> = RepositoryImpl(dynamoDB, mapper, userRepository)

	fun createUser(){
		val user1 : User = User(
			id = "USER49",
			name = "Varun",
			workspaceIdentifier = WorkspaceIdentifier("WORKSPACE1234")
		)

		val user2 : User = User(
			id = "USER49",
			name = "Varun",
			namespaceIdentifier = NamespaceIdentifier("NAMESPACE1")
		)
		repository.create(user2)

	}

	fun getUser(){
		val user : Entity = repository.get(UserIdentifier("USER49"))
		println(user)
	}

	fun updateUser(){
		val user : User = User(
			id = "USER49",
			name = "Varun Garg",
			workspaceIdentifier = WorkspaceIdentifier("WS1234")
		)
		repository.update(user)
	}

	fun deleteUser(){
		repository.delete(UserIdentifier("USER49"))
	}

	fun getAllUsersByWorkspaceID(){
		val workspaceID = "WORKSPACE1234"
		val workspaceIdentifier = WorkspaceIdentifier(workspaceID)
		userRepository.getAllUsersWithWorkspaceID(workspaceIdentifier)
	}

	fun getAllUsersByNamespaceID(){
		val namespaceID = "NAMESPACE1"
		val namespaceIdentifier = NamespaceIdentifier(namespaceID)
		userRepository.getAllUsersWithNamespaceID(namespaceIdentifier)
	}

}


fun main(){
	//UserService().createUser()
	//UserService().getUser()
	//UserService().updateUser()
	//UserService().deleteUser()
	UserService().getAllUsersByNamespaceID()
	UserService().getAllUsersByWorkspaceID()
}
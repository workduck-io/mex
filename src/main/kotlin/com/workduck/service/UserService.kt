package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.*
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.UserRepository
import com.workduck.utils.DDBHelper

class UserService {
	private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
	private val dynamoDB: DynamoDB = DynamoDB(client)
	private val mapper = DynamoDBMapper(client)

	private val userRepository: UserRepository = UserRepository(dynamoDB, mapper)
	private val repository: Repository<User> = RepositoryImpl(dynamoDB, mapper, userRepository)

	fun createUser(jsonString : String) {

		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val user: User = objectMapper.readValue(jsonString)

		/* since idCopy is SK for Namespace object, it can't be null if not sent from frontend */
		user.idCopy = user.id

		repository.create(user)

	}

	fun getUser(userID : String) : String {
		val user: Entity = repository.get(UserIdentifier(userID))
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		return objectMapper.writeValueAsString(user)
	}

	fun updateUser(jsonString: String) {
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val user: User = objectMapper.readValue(jsonString)

		/* since idCopy is SK for Namespace object, it can't be null if not sent from frontend */
		user.idCopy = user.id

		/* to avoid updating createdAt un-necessarily */
		user.createdAt = null

		repository.update(user)
	}

	fun deleteUser(userID: String) {
		repository.delete(UserIdentifier(userID))
	}

	fun getAllUsersByWorkspaceID() {
		val workspaceID = "WORKSPACE1234"
		val workspaceIdentifier = WorkspaceIdentifier(workspaceID)
		userRepository.getAllUsersWithWorkspaceID(workspaceIdentifier)
	}

	fun getAllUsersByNamespaceID() {
		val namespaceID = "NAMESPACE1"
		val namespaceIdentifier = NamespaceIdentifier(namespaceID)
		userRepository.getAllUsersWithNamespaceID(namespaceIdentifier)
	}

}


fun main() {

	val json : String = """
		{
			"id" : "USER49",
			"name" : "Varun",
			"email" : "varun.iitp@gmail.com"		
		}
		"""

	val jsonUpdated : String = """
		{
			"id" : "USER49",
			"name" : "Varun Garg",
			"email" : "varun.garg@workduck.io"
		}
		"""

	//UserService().createUser(json)
	//println(UserService().getUser("USER49"))
	//UserService().updateUser(jsonUpdated)
	UserService().deleteUser("USER49")
	//UserService().getAllUsersByNamespaceID()
	//UserService().getAllUsersByWorkspaceID()
}
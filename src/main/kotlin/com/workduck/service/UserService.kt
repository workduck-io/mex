package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.User
import com.workduck.models.Entity
import com.workduck.models.UserIdentifier
import com.workduck.models.IdentifierType
import com.workduck.models.Identifier
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.UserRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class UserService {
	private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
	private val dynamoDB: DynamoDB = DynamoDB(client)
	private val mapper = DynamoDBMapper(client)

	private val tableName: String = when(System.getenv("TABLE_NAME")) {
		null -> "local-mex" /* for local testing without serverless offline */
		else -> System.getenv("TABLE_NAME")
	}

	private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
		.withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
		.build()

	private val userRepository: UserRepository = UserRepository(dynamoDB, mapper, dynamoDBMapperConfig)
	private val repository: Repository<User> = RepositoryImpl(dynamoDB, mapper, userRepository, dynamoDBMapperConfig)

    fun createUser(jsonString: String): Entity? {

		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val user: User = objectMapper.readValue(jsonString)

		/* since idCopy is SK for Namespace object, it can't be null if not sent from frontend */
		user.idCopy = user.id
		LOG.info("Creating user : $user")
		return repository.create(user)
	}

	fun getUser(userID : String) : Entity? {
		LOG.info("Getting user with id : $userID")
		return repository.get(UserIdentifier(userID))
	}

	fun updateUser(jsonString: String) : Entity? {
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val user: User = objectMapper.readValue(jsonString)

		/* since idCopy is SK for Namespace object, it can't be null if not sent from frontend */
		user.idCopy = user.id

		/* to avoid updating createdAt un-necessarily */
		user.createdAt = null

		return repository.update(user)
	}

	fun registerUser(jsonString: String, workspaceName: String?): Entity?{
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val user: User = objectMapper.readValue(jsonString)

		val workspaceID = Helper.generateId(Helper.generateId(IdentifierType.WORKSPACE.name))
		val jsonForWorkspaceCreation : String = """{
			"id": "$workspaceID",
			"name": "$workspaceName"
		}"""

		LOG.info("Creating workspace with json : $jsonForWorkspaceCreation")

		return WorkspaceService().createWorkspace(jsonForWorkspaceCreation)

	}


	fun deleteUser(userID: String) : Identifier? {
		return repository.delete(UserIdentifier(userID))
	}

	fun getAllUsersWithWorkspaceID(workspaceID : String) : MutableList<String>? {
		return userRepository.getAllUsersWithWorkspaceID(workspaceID)
	}

	fun getAllUsersWithNamespaceID(namespaceID : String) : MutableList<String>? {
		return userRepository.getAllUsersWithNamespaceID(namespaceID)
	}

	companion object {
		private val LOG = LogManager.getLogger(UserService::class.java)
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

	UserService().registerUser(json, "WD")
	//UserService().createUser(json)
	//println(UserService().getUser("USER49"))
	//UserService().updateUser(jsonUpdated)
	//UserService().deleteUser("USER49")
	//println(UserService().getAllUsersWithNamespaceID("NAMESPACE1"))
	//UserService().getAllUsersByWorkspaceID()
}
package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Identifier
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.UserIdentifierRecord
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.UserIdentifierMappingRepository
import com.workduck.utils.DDBHelper

class UserIdentifierMappingService {
	private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
	private val dynamoDB: DynamoDB = DynamoDB(client)
	private val mapper = DynamoDBMapper(client)
	private val userIdentifierMappingRepository: UserIdentifierMappingRepository = UserIdentifierMappingRepository(dynamoDB, mapper)
	private val repository: Repository<UserIdentifierRecord> = RepositoryImpl(dynamoDB, mapper, userIdentifierMappingRepository)


	fun createUserIdentifierRecord(jsonString: String){
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val userIdentifierRecord: UserIdentifierRecord = objectMapper.readValue(jsonString)
		repository.create(userIdentifierRecord)

	}

	/* returns user details data + user mapping with namespace + user mapping with workspace */
	fun getUserRecords(userID : String){
		userIdentifierMappingRepository.getRecordsByUserID(userID)
	}


	fun deleteUserIdentifierMapping(userID: String, identifierID : String){
		if(identifierID.startsWith("NAMESPACE"))
			userIdentifierMappingRepository.deleteUserIdentifierMapping(userID, NamespaceIdentifier(identifierID))
		else
			userIdentifierMappingRepository.deleteUserIdentifierMapping(userID, WorkspaceIdentifier(identifierID))
	}



}

fun main(){
	val json : String = """
		{
			"userID" : "USER49",
			"identifier" : "NAMESPACE1"
		}
		"""
	//UserIdentifierMappingService().createUserIdentifierRecord(json)
	//UserIdentifierMappingService().getUserRecords("USER49")
	//UserIdentifierMappingService().deleteUserIdentifierMapping("USER49", "NAMESPACE1")
}
package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*

class NamespaceRepository(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper,
	private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Namespace> {

	private val tableName: String = when(System.getenv("TABLE_NAME")) {
		null -> "local-mex" /* for local testing without serverless offline */
		else -> System.getenv("TABLE_NAME")
	}

	override fun get(identifier: Identifier): Entity? {
		return try {
			return mapper.load(Namespace::class.java, identifier.id, identifier.id)
		} catch (e : Exception){
			null
		}
	}

	override fun create(t: Namespace): Namespace {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier) : String? {
		val table = dynamoDB.getTable(tableName)

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", identifier.id, "SK", identifier.id)

		return try {
			table.deleteItem(deleteItemSpec)
			identifier.id
		} catch ( e : Exception){
			null
		}
	}

	override fun update(t: Namespace): Namespace {
		TODO("Not yet implemented")
	}

	fun getNamespaceData(namespaceIDList : List<String>) : MutableList<String>? {
		val namespaceJsonList : MutableList<String>  = mutableListOf()
		val objectMapper = ObjectMapper()
		return try {
			for (namespaceID in namespaceIDList) {
				val namespace: Namespace? = mapper.load(Namespace::class.java, namespaceID, namespaceID)
				if (namespace != null) {
					val namespaceJson = objectMapper.writeValueAsString(namespace)
					namespaceJsonList += namespaceJson
				}
			}
			namespaceJsonList
		} catch (e : Exception){
			null
		}
		TODO("we also need to have some sort of filter which filters out all the non-namespace ids")
		TODO("this code can be reused for similar workspace functionality")
	}

}
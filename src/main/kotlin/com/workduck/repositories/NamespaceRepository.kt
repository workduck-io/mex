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

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Namespace::class.java, identifier.id, identifier.id)
	}

	override fun create(t: Namespace): Namespace {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier) {
		val table = dynamoDB.getTable(System.getenv("TABLE_NAME"))

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", identifier.id, "SK", identifier.id)

		table.deleteItem(deleteItemSpec)
	}

	override fun update(t: Namespace): Namespace {
		TODO("Not yet implemented")
	}

	fun getNamespaceData(namespaceIDList : List<String>) : MutableList<String>{
		val namespaceJsonList : MutableList<String>  = mutableListOf()
		val objectMapper = ObjectMapper()
		for(namespaceID in namespaceIDList ) {
			val namespace : Namespace? = mapper.load(Namespace::class.java, namespaceID, namespaceID)
			if(namespace!=null) {
				val namespaceJson = objectMapper.writeValueAsString(namespace)
				namespaceJsonList += namespaceJson
			}
		}
		return namespaceJsonList
		TODO("we also need to have some sort of filter which filters out all the non-namespace ids")
		TODO("this code can be reused for similar workspace functionality")
	}

}
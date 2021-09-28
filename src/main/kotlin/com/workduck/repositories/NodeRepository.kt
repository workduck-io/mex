package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*

class NodeRepository(
	private val mapper: DynamoDBMapper,
	private val dynamoDB: DynamoDB
) : Repository<Node>  {

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Node::class.java, identifier.id)
	}

	fun append(identifier: Identifier, tableName: String, elements : MutableList<Element>) {
		val table = dynamoDB.getTable(tableName)

		val objectMapper = ObjectMapper()
		val elementsInStringFormat : MutableList<String> = mutableListOf()
		for(e in elements){
			val entry : String = objectMapper.writeValueAsString(e)
			elementsInStringFormat += entry
		}

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":val1"] = elementsInStringFormat
		expressionAttributeValues[":empty_list"] = mutableListOf<Element>()


		val updateItemSpec : UpdateItemSpec = UpdateItemSpec().withPrimaryKey("PK", identifier.id)
			.withUpdateExpression("set SK = list_append(if_not_exists(SK, :empty_list), :val1)")
			.withValueMap(expressionAttributeValues)


		table.updateItem(updateItemSpec)
	}

	override fun create(t: Node): Node {
		TODO("Not yet implemented")
	}

	override fun update(t: Node): Node {
		TODO("Not yet implemented")
	}


	override fun delete(identifier: Identifier, tableName: String) {
		TODO("Not yet implemented")
	}

}
package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.models.Entity
import com.workduck.models.Identifier


class RepositoryImpl<T : Entity>(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper,
	private val repository: Repository<T>,
	private val dynamoDBMapperConfig: DynamoDBMapperConfig
) : Repository<T> {

	override fun get(identifier: Identifier): Entity {
		val tableName: String = System.getenv("TABLE_NAME")

		return repository.get(identifier)
	}

	override fun delete(identifier: Identifier) {
		repository.delete(identifier)
	}

	override fun create(t: T): T {
		mapper.save(t, dynamoDBMapperConfig)
		return t
	}

	override fun update(t: T): T {

		val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
			.withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
			.withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
			.withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(System.getenv("TABLE_NAME")))
			.build()

		mapper.save(t, dynamoDBMapperConfig)
		return t;
	}

}


/*
	override fun create(node: Node): Node {
		// TODO("Not yet implemented")
		val table: Table = dynamoDB.getTable("elementsTable")

		for( element in node.data){

			val children : List<Element> = element.getChildren()
			val myMap = mutableMapOf<String,String>()

			for( child in children ){
				//map.put("CHILD#${child.getID()}")
				myMap["CHILD#${child.getID()}"] = "Content#" + child.content() + "Type#" + child.getElementType()

			}

			val item : Item = Item()
				.withPrimaryKey("PK", node.id)
				.withString("SK", "PARENT#${element.getID()}")
				.withMap("ChildrenInfo", myMap)
				.withString("ParentElementType", element.getElementType())

			table.putItem(item)
		}

		table.putItem(node)
		return node
	}
	*/


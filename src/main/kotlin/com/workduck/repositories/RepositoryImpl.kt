package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.Element
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Node
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


class RepositoryImpl<T: Entity>(
    //val amazonDynamoDB: AmazonDynamoDB
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper
): Repository<T> {
    override fun get(identifier: Identifier): Node {

        /* How to avoid explicitly writing Node class here??? */
        return mapper.load(Node::class.java, identifier.id)

    }

    override fun delete(identifier: Identifier, tableName : String) {

        val table = dynamoDB.getTable(tableName)

        /* currently there's just one record per primary key hence this is feasible. Will need to change this in future */
        val deleteItemSpec : DeleteItemSpec =  DeleteItemSpec().withPrimaryKey("PK", identifier.id)

        table.deleteItem(deleteItemSpec)

    }

    override fun create(t: T): T {
        mapper.save(t)
        return t
    }

    override fun update(t: T): T {
        mapper.save(t)
        return t;
    }

    override fun append(identifier: Identifier, tableName: String, elements : MutableList<Element>) {
        val table = dynamoDB.getTable(tableName)

        //val map: ValueMap = ValueMap().withList(":img",elements)
        //val objectMapper = ObjectMapper()
       // val map: Map<String, Any> = objectMapper.convertValue(elements, MutableMap::class.java)

       // val valueMap = ValueMap.withList(":val1", Arrays.asList(elements))
        val objectMapper = ObjectMapper()
        val elementsInStringFormat : MutableList<String> = mutableListOf()
        for(e in elements){
            val entry : String = objectMapper.writeValueAsString(e)
            elementsInStringFormat += entry
        }

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":val1"] = elementsInStringFormat
        expressionAttributeValues[":empty_list"] = mutableListOf<Element>()

        //val map: List<Element>? = objectMapper.convertValue(elements, List<Element>::class.java)
        val updateItemSpec : UpdateItemSpec = UpdateItemSpec().withPrimaryKey("PK", identifier.id)
            .withUpdateExpression("set SK = list_append(if_not_exists(SK, :empty_list), :val1)")
            .withValueMap(expressionAttributeValues)


        table.updateItem(updateItemSpec)
    }



//    override fun create(node: Node): Node {
//        // TODO("Not yet implemented")
//        val table: Table = dynamoDB.getTable("elementsTable")
//
//        for( element in node.data){
//
//            val children : List<Element> = element.getChildren()
//            val myMap = mutableMapOf<String,String>()
//
//            for( child in children ){
//                //map.put("CHILD#${child.getID()}")
//                myMap["CHILD#${child.getID()}"] = "Content#" + child.content() + "Type#" + child.getElementType()
//
//            }
//
//            val item : Item = Item()
//                .withPrimaryKey("PK", node.id)
//                .withString("SK", "PARENT#${element.getID()}")
//                .withMap("ChildrenInfo", myMap)
//                .withString("ParentElementType", element.getElementType())
//
//            table.putItem(item)
//        }

       // table.putItem(node)
//        return node
//    }


}



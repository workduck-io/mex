package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*
import kotlin.collections.HashMap


class RepositoryImpl<T: Entity>(
    //val amazonDynamoDB: AmazonDynamoDB
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper
): Repository<T> {
    override fun get(identifier: Identifier): Entity {

        /* How to avoid explicitly writing Node, Workspace classes here??? */
        if(identifier is NodeIdentifier)
            return mapper.load(Node::class.java, identifier.id)


        return mapper.load(Workspace::class.java, identifier.id)


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

    /* Should we create NodeRepositoryImpl and implement core functionality of this function there? */
    override fun append(identifier: Identifier, tableName: String, elements : MutableList<Element>) {
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



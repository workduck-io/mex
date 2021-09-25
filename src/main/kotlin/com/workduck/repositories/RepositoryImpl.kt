package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Node


class RepositoryImpl<T: Entity>(
    //val amazonDynamoDB: AmazonDynamoDB
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper
): Repository<T> {
    override fun get(identifier: Identifier) {

        /* How to avoid explicitly writing Node class here??? */
        val node: Node =  mapper.load(Node::class.java, identifier.id)
        println(node)
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


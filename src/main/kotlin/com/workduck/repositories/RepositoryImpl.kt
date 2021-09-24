package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Node
import java.awt.print.Book


class RepositoryImpl<T: Entity>(
    //val amazonDynamoDB: AmazonDynamoDB
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper
): Repository<T> {
    override fun get(identifier: Identifier) {

        /* How to avoid explicitly writing Node class here??? */

        /* Not Working????????????!!!! */
        val node: Node =  mapper.load(Node::class.java, identifier.id)

        println(node)
        //return node
       // return nodeRetrieved

    }

    override fun delete(identifier: Identifier) {
        TODO("Not yet implemented")
    }

    override fun create(t: T): T {
        val table: Table = dynamoDB.getTable("elementsTable")
       // println(t.sortKey + "" +  t.partitionKey)


        mapper.save(t)
        return t
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

    override fun update(t: T): T {
        TODO("Not yet implemented")
    }
}
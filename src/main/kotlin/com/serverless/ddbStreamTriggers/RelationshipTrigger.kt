package com.serverless.ddbStreamTriggers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.workduck.service.WorkspaceService
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.serverless.nodeHandlers.NodeHandler
import org.apache.logging.log4j.LogManager


class RelationshipTrigger : RequestHandler<DynamodbEvent, Void> {
    private val workspaceService = WorkspaceService()

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        println("DYNAMODB-EVENT : $dynamodbEvent")
        for (record in dynamodbEvent?.records!!) {

            val newNodeHierarchyInformation =
                    getNodeHierarchyInformationFromImage(record.dynamodb.newImage["nodeHierarchyInformation"]?.l ?: listOf())

            val oldNodeHierarchyInformation =
                    getNodeHierarchyInformationFromImage(record.dynamodb.oldImage["nodeHierarchyInformation"]?.l ?: listOf())

            LOG.info("NEW : $newNodeHierarchyInformation")
            LOG.info("OLD : $oldNodeHierarchyInformation")

            val addedPath = getDifferenceOfList(newNodeHierarchyInformation, oldNodeHierarchyInformation)

            val removedPath = getDifferenceOfList(oldNodeHierarchyInformation, newNodeHierarchyInformation)

            /* case when more nodes added on an existing path */
            if(addedPath.size == 1 && removedPath.size == 1) {
                addedPath[0].removePrefix(removedPath[0].commonPrefixWith(addedPath[0]))

            } else if(addedPath.size == 1 && removedPath.isEmpty()){

            } else {
                throw Exception("Invalid case")
            }






        }
        return null
    }

    private fun getNodeHierarchyInformationFromImage(listOfAttributeValue: List<AttributeValue>) : List<String>{
        val nodeHierarchyInformation = mutableListOf<String>()

        for(nodePath in listOfAttributeValue){
            nodeHierarchyInformation.add(nodePath.s)
        }
        return nodeHierarchyInformation
    }

    private fun getDifferenceOfList(firstList: List<String>, secondList: List<String>) : List<String> {

        /* return items in first list but not in second */
        return firstList.filterNot { secondList.contains(it) }

    }

    companion object {
        private val LOG = LogManager.getLogger(RelationshipTrigger::class.java)
    }
}





//
//override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
//    for (record in dynamodbEvent?.records!!) {
//
//        when(val oldImage = record.dynamodb.oldImage){
//            null -> continue
//            else -> {
//                RelationshipHelper.handleRelationDeletion(oldImage["SK"]?.s, oldImage["endNode"]?.s, oldImage["workspaceIdentifier"]?.s, workspaceService)
//            }
//        }
//
//        when(val newImage = record.dynamodb.newImage){
//            null -> continue
//            else -> {
//                RelationshipHelper.handleRelationAddition(newImage["SK"].s, newImage["endNode"]?.s, newImage["workspaceIdentifier"]?.s, workspaceService)
//            }
//        }
//
//
//
//        val x = record.dynamodb.newImage
//
//        val startNode = x["SK"]?.s
//
//        val endNode = x["endNode"]?.s
//
//
//
//
//
//        val dynamoDbAttributes: Map<String, AttributeValue> = Helper.objectMapper.convertValue(x, object : TypeReference<Map<String?, AttributeValue?>?>() {})
//
//        val mp = ItemUtils.toItem(dynamoDbAttributes).toJSON()
//
//        val r : Relationship = Helper.objectMapper.readValue(mp)
//
//        println("Relationship : $r")
//
//
////            println("1" + record.dynamodb)
////            println("2" + record.eventID)
////            println("3" + record.eventName)
////            println("4" + record.dynamodb.toString())
//    }
//
//    return null
//}
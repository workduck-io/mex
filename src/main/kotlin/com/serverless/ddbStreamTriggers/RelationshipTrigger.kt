package com.serverless.ddbStreamTriggers

import com.amazonaws.services.dynamodbv2.document.ItemUtils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.utils.RelationshipHelper
import com.workduck.models.Relationship
import com.workduck.service.WorkspaceService
import com.workduck.utils.Helper


class RelationshipTrigger : RequestHandler<DynamodbEvent, Void> {
    private val workspaceService = WorkspaceService()

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        for (record in dynamodbEvent?.records!!) {

            when(val oldImage = record.dynamodb.oldImage){
                null -> continue
                else -> {
                    RelationshipHelper.handleRelationDeletion(oldImage["SK"]?.s, oldImage["endNode"]?.s, oldImage["workspaceIdentifier"]?.s, workspaceService)
                }
            }

            when(val newImage = record.dynamodb.newImage){
                null -> continue
                else -> {
                    RelationshipHelper.handleRelationAddition(newImage["SK"].s, newImage["endNode"]?.s, newImage["workspaceIdentifier"]?.s, workspaceService)
                }
            }



            val x = record.dynamodb.newImage

            val startNode = x["SK"]?.s

            val endNode = x["endNode"]?.s





            val dynamoDbAttributes: Map<String, AttributeValue> = Helper.objectMapper.convertValue(x, object : TypeReference<Map<String?, AttributeValue?>?>() {})

            val mp = ItemUtils.toItem(dynamoDbAttributes).toJSON()

            val r : Relationship = Helper.objectMapper.readValue(mp)

            println("Relationship : $r")


//            println("1" + record.dynamodb)
//            println("2" + record.eventID)
//            println("3" + record.eventName)
//            println("4" + record.dynamodb.toString())
        }

        return null
    }
}
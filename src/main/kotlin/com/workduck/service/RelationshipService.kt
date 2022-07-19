package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.workduck.models.ItemStatus
import com.workduck.models.Relationship
import com.workduck.repositories.RelationshipRepository
import com.workduck.utils.DDBHelper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.models.RelationshipType


class RelationshipService (

        val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
        val dynamoDB: DynamoDB = DynamoDB(client),
        val mapper: DynamoDBMapper = DynamoDBMapper(client),

        var tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    },

        var dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build(),

        val relationshipRepository: RelationshipRepository = RelationshipRepository(mapper, dynamoDBMapperConfig, dynamoDB, tableName)

) {
    fun createRelationshipInBatch(list: List<Relationship>) {
        println(tableName)
        relationshipRepository.createInBatch(list)
    }

    fun getHierarchyRelationshipsOfWorkspace(workspaceID: String, status: ItemStatus): List<Relationship> {
        return relationshipRepository.getHierarchyRelationshipsOfWorkspace(workspaceID, status)
    }

    fun getHierarchyRelationshipsWithStartNode(workspaceID: String, startNodeID: String, status: ItemStatus): List<Relationship> {
        return relationshipRepository.getHierarchyRelationshipsWithStartNode(workspaceID, startNodeID, status)
    }

    fun getHierarchyRelationshipsWithEndNode(workspaceID: String, startNodeID: String, status: ItemStatus): List<Relationship> {
        return relationshipRepository.getHierarchyRelationshipsWithEndNode(workspaceID, startNodeID, status)
    }

    fun changeRelationshipStatus(list: List<Relationship>, status: ItemStatus) {
        relationshipRepository.changeRelationshipStatus(list, status)
    }


    fun getRelationship(startNode: String, endNode: String, workspaceID: String, relationshipType : RelationshipType): Relationship?{
        return relationshipRepository.getRelationship(startNode, endNode, workspaceID, relationshipType)
    }

    fun deleteRelationship(relationship: Relationship?){
        relationship?.let { relationshipRepository.deleteRelationship(relationship) }
    }


}

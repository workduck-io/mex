package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.TagRequest
import com.serverless.models.requests.WDRequest
import com.workduck.models.NodeIdentifier
import com.workduck.models.Tag
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.TagRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class TagService(
        private val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
        private val dynamoDB: DynamoDB = DynamoDB(client),
        private val mapper: DynamoDBMapper = DynamoDBMapper(client),

        private val tableName: String = DDBHelper.getTableName(),

        var dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

        val table: Table = dynamoDB.getTable(tableName),

        private val tagRepository: TagRepository = TagRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName),
) {




    fun addNodeForTags(wdRequest: WDRequest, workspaceID: String) = runBlocking {
        val tagNameSet = (wdRequest as TagRequest).tagNames
        val nodeID = wdRequest.nodeID

        val existingTags = tagRepository.batchGetTags(tagNameSet, workspaceID)
        LOG.debug(existingTags)
        val tagsToBeCreated = getTagObjectsFromNameList(getTagNamesToBeCreated(tagNameSet, existingTags.map{ it.name }), nodeID, workspaceID)
        LOG.debug(tagsToBeCreated)
        launch { tagRepository.batchCreateTags(tagsToBeCreated) }
        updateExistingTags(existingTags, nodeID, workspaceID)
    }


    fun deleteNodeFromTags(wdRequest: WDRequest, workspaceID: String) {
        val nodeID = (wdRequest as TagRequest).nodeID
        wdRequest.tagNames.map { tagRepository.deleteNodeFromTag(it, nodeID, workspaceID) }
    }


    fun getAllNodesByTag(tagName: String, workspaceID: String) : List<String> {
        return getTag(tagName, workspaceID)?.nodes?.map {  it.id } ?: emptyList()
    }


    fun getAllTagsOfWorkspace(workspaceID: String) : List<String> {
        return tagRepository.getAllTagsOfWorkspace(workspaceID)

    }


    fun getTag(tagName: String, workspaceID: String) : Tag?{
        return tagRepository.getTag(tagName, workspaceID)
    }

    private fun updateExistingTags(existingTags: List<Tag>, nodeID: String, workspaceID: String){
        existingTags.map {  tagRepository.updateExistingTag(it, nodeID, workspaceID) }
    }

    private fun getTagNamesToBeCreated(listOfNames : Set<String>, existingNames: List<String>) : List<String> {
        return listOfNames.toList().minus(existingNames.toSet())
    }

    private fun getTagObjectsFromNameList(tagNameList: List<String>, nodeID: String, workspaceID: String) : List<Tag> {
        return tagNameList.map { Tag(
                name = it,
                workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                nodes = listOf(NodeIdentifier(nodeID))
        ) }
    }

    fun save(tag: Tag){
        mapper.save(tag, dynamoDBMapperConfig)
    }

    companion object {
        private val LOG = LogManager.getLogger(TagService::class.java)
    }
}

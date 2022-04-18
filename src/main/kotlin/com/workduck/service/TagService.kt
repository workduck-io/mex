package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.workduck.models.NodeIdentifier
import com.workduck.models.Tag
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.TagRepository
import com.workduck.utils.DDBHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class TagService(
        private val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
        private val dynamoDB: DynamoDB = DynamoDB(client),
        private val mapper: DynamoDBMapper = DynamoDBMapper(client),

        private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    },

        var dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

        val table: Table = dynamoDB.getTable(tableName),

        private val tagRepository: TagRepository = TagRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName),
) {

    fun addNodeForTag(tagNameList: List<String>, nodeID: String, workspaceID: String) = runBlocking {
        val existingTags = tagRepository.batchGetTags(tagNameList, workspaceID)
        LOG.info(existingTags)
        val tagsToBeCreated = getTagObjectsFromNameList(getTagNamesToBeCreated(tagNameList, existingTags.map{ it.name }), nodeID, workspaceID)
        LOG.info(tagsToBeCreated)
        launch { tagRepository.batchCreateTags(tagsToBeCreated) }
        updateExistingTags(existingTags, nodeID, workspaceID)
    }


    fun deleteNodeFromTag(tagNameList: List<String>, nodeID: String, workspaceID: String) {

        tagNameList.map { tagRepository.deleteNodeFromTag(it, nodeID, workspaceID) }

    }


    fun getAllNodesByTag(tagName: String, workspaceID: String) : List<String>? {
        return getTag(tagName, workspaceID).nodes?.map {  it.id }
    }


    fun getAllTagsOfWorkspace(workspaceID: String) : List<String> {
        return tagRepository.getAllTagsOfWorkspace(workspaceID)

    }


    fun getTag(tagName: String, workspaceID: String) : Tag{
        return tagRepository.getTag(tagName, workspaceID)
    }

    private fun updateExistingTags(existingTags: List<Tag>, nodeID: String, workspaceID: String){
        existingTags.map {  tagRepository.updateExistingTag(it, nodeID, workspaceID) }
    }

    private fun getTagNamesToBeCreated(listOfNames : List<String>, existingNames: List<String>) : List<String> {
        return listOfNames.minus(existingNames.toSet())
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


fun main(){
    val tag = Tag(
            name = "2 - XYZ",
            workspaceIdentifier = WorkspaceIdentifier("WORKSPACE2")
    )

    //println(listOf(1, 2, 4).minus(listOf(4, 1)))

    //TagService().save(tag)
    //TagService().addTagForNode(listOf("XYZ"), "", "WORKSPACE1")
    //TagService().addNodeForTag(listOf("ABC", "GGWP"), "NODE1", "WORKSPACE1")

    //TagService().deleteNodeFromTag(listOf("ABC"), "NODE1", "WORKSPACE1")

}


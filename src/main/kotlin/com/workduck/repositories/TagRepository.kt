package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ReturnValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.utils.Constants
import com.workduck.models.Element
import com.workduck.models.ItemType
import com.workduck.models.Node
import com.workduck.models.Tag
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.utils.Helper
import com.workduck.utils.TagHelper.convertObjectToTag
import com.workduck.utils.TagHelper.getNodesMapFromOutcomeObject


class TagRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB,
        private val tableName: String

) {


    fun getTag(tagName: String, workspaceID: String): Tag?{
        return mapper.load(Tag::class.java, WorkspaceIdentifier(workspaceID), tagName, dynamoDBMapperConfig)
    }

    fun getAllTagsOfWorkspace(workspaceID: String) : List<String> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":tag"] = AttributeValue().withS(ItemType.Tag.name)

        return DynamoDBQueryExpression<Tag>().query(keyConditionExpression = "PK = :pk", filterExpression = "itemType = :tag",
                projectionExpression = "SK",  expressionAttributeValues = expressionAttributeValues).let {
            mapper.query(Tag::class.java, it, dynamoDBMapperConfig)
        }.map { it.name }

    }

    fun batchGetTags(tagNameList: Set<String>, workspaceID: String) : List<Tag> {
        val keyPairList = tagNameList.map {
            val keyPair = KeyPair()
            keyPair.withHashKey(WorkspaceIdentifier(workspaceID))
            keyPair.withRangeKey(it)
        }

        val keyPairForTable = HashMap<Class<*>, List<KeyPair>>()
        keyPairForTable[Tag::class.java] = keyPairList

        val batchResults: Map<String, List<Any>> = mapper.batchLoad(keyPairForTable, dynamoDBMapperConfig)

        val listOfTags = mutableListOf<Tag>()
        for ((_, tagList) in batchResults) {
            for(tag in tagList){
                listOfTags.add(convertObjectToTag(tag))
            }
        }
        return listOfTags
    }

    fun batchCreateTags(tagList : List<Tag>){
        val failedBatches = mapper.batchWrite(tagList, emptyList<Any>(), dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }

    fun updateExistingTag(tag: Tag, nodeID: String, workspaceID: String){
        val table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()
        expressionAttributeValues[":nodeID"] = nodeID

        val updateExpression = "set updatedAt = :updatedAt, nodes.${nodeID} = :nodeID"
        val conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"

        UpdateItemSpec().update(pk = workspaceID, sk = tag.name,
                updateExpression = updateExpression, expressionAttributeValues = expressionAttributeValues,
                conditionExpression = conditionExpression).let {
                    table.updateItem(it)
        }

    }

    fun deleteNodeFromTag(tagName: String, nodeID: String, workspaceID: String){
        val table = dynamoDB.getTable(tableName)

        val updateExpression = "SET updatedAt = :updatedAt REMOVE nodes.${nodeID}"
        val conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()

        val newValues = UpdateItemSpec().updateWithReturnValues(pk = workspaceID, sk = tagName, updateExpression = updateExpression, expressionAttributeValues = expressionAttributeValues,
                                conditionExpression = conditionExpression, returnValue = ReturnValue.ALL_NEW).let {
            table.updateItem(it)
        }


        if(getNodesMapFromOutcomeObject(newValues).isEmpty()){
            DeleteItemSpec().withPrimaryKey("PK", workspaceID, "SK", tagName).let {
                        table.deleteItem(it) }
        }

    }

}
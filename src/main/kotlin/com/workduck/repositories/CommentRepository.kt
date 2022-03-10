package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.workduck.models.Comment
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class CommentRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB,
        private val tableName : String
) : Repository<Comment> {

    val table: Table = dynamoDB.getTable(tableName)


    override fun create(t: Comment): Comment? {
        TODO("Not yet implemented")
    }

    override fun update(t: Comment): Comment? {
        TODO("Not yet implemented")
    }

    override fun get(identifier: Identifier): Entity? {
        TODO("Using getComment instead")
    }

    override fun delete(identifier: Identifier): Identifier? {
        TODO("Using deleteComment instead")
    }


    fun getComment(pk: String, sk: String) : Comment? {
        return mapper.load(Comment::class.java, pk, sk, dynamoDBMapperConfig)
    }

    fun updateComment(comment : Comment) {

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":body"] = Helper.objectMapper.writeValueAsString(comment.commentBody)
        expressionAttributeValues[":updatedAt"] = comment.updatedAt
        expressionAttributeValues[":currentCommenter"] = comment.commentedBy?.id.toString()


        UpdateItemSpec().withPrimaryKey("PK", comment.pk, "SK", comment.sk)
                .withUpdateExpression("set commentBody = :body , updatedAt = :updatedAt")
                .withValueMap(expressionAttributeValues)
                .withConditionExpression("AK = :currentCommenter").let{
                    table.updateItem(it)
                }

    }

    fun deleteComment(pk: String, sk: String) {


        DeleteItemSpec()
                .withPrimaryKey("PK", pk, "SK", sk).let {
                    table.deleteItem(it)
                }
    }


    fun getAllCommentsOfNodeOrBlock(pk : String) : List<Comment> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(pk)

        return DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("PK = :pk")
                .withExpressionAttributeValues(expressionAttributeValues).let{
                    mapper.query(Comment::class.java, it, dynamoDBMapperConfig)
                }

    }

    fun getAllCommentsOfUser(userID : String) : List<Comment>{
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":ak"] = AttributeValue(userID)
        expressionAttributeValues[":itemType"] = AttributeValue("Comment")


        return DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("AK = :ak  and itemType = :itemType")
                .withIndexName("itemType-AK-index").withConsistentRead(false)
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(Comment::class.java, it, dynamoDBMapperConfig)
                }
    }

    companion object {
        private val LOG = LogManager.getLogger(CommentRepository::class.java)
    }



}
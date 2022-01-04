package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.workduck.models.Comment
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.UserPreferenceRecord
import org.apache.logging.log4j.LogManager

class CommentRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB,
        private val tableName : String
) : Repository<Comment> {
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

    fun deleteComment(pk: String, sk: String){
        val table = dynamoDB.getTable(tableName)

        val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
                .withPrimaryKey("PK", pk, "SK", sk)

        try {
            table.deleteItem(deleteItemSpec)
            LOG.info("Deleted the comment")
        } catch (e: Exception) {
            LOG.info(e)
        }
    }


    fun getAllCommentsOfBlock(pk : String, blockID : String?) : List<Comment> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(pk)
        expressionAttributeValues[":sk"] = AttributeValue().withS("$blockID#")

         return DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("PK = :pk and SK begins_with :sk")
                .withExpressionAttributeValues(expressionAttributeValues).let{
                    mapper.query(Comment::class.java, it, dynamoDBMapperConfig)
                }
    }


    fun getAllCommentsOfNode(pk : String) : List<Comment> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(pk)

        return DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("PK = :pk")
                .withExpressionAttributeValues(expressionAttributeValues).let{
                    mapper.query(Comment::class.java, it, dynamoDBMapperConfig)
                }

    }

    companion object {
        private val LOG = LogManager.getLogger(CommentRepository::class.java)
    }


}
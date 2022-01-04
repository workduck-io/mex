package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.workduck.models.Comment
import com.workduck.models.Entity
import com.workduck.models.Identifier
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


    fun getAllCommentsOfBlock(){

    }


    fun getAllCommentsOfNode(){

    }

    companion object {
        private val LOG = LogManager.getLogger(CommentRepository::class.java)
    }


}
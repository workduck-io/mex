package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.CommentRequest
import com.serverless.models.WDRequest
import com.workduck.models.Comment
import com.workduck.repositories.CommentRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper

class CommentService {

    private val objectMapper = Helper.objectMapper
    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build()


    private val commentRepository: CommentRepository = CommentRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName)
    private val repository: Repository<Comment> = RepositoryImpl(dynamoDB, mapper, commentRepository, dynamoDBMapperConfig)

    fun getComment(nodeID: String?, blockID: String?, commentID: String?) : Comment? {
        val pk = generatePK(nodeID)
        val sk = generateSK(blockID, commentID)
        return commentRepository.getComment(pk, sk)
    }

    fun createComment(commentRequest : WDRequest?) : Comment? {
        val comment = createCommentObjectFromCommentRequest(commentRequest as CommentRequest?) ?: return null
        return repository.create(comment)
    }

    fun updateComment(commentRequest: WDRequest?) : Comment? {
        val comment = createCommentObjectFromCommentRequest(commentRequest as CommentRequest?) ?: return null
        comment.createdAt = null
        return repository.update(comment)
    }

    fun deleteComment(nodeID: String?, blockID : String?, commentID : String?) {
        val pk = generatePK(nodeID)
        val sk = generateSK(blockID, commentID)
        commentRepository.deleteComment(pk, sk)
    }



    private fun generatePK(nodeID: String?) : String{
        return "$nodeID#COMMENT"
    }

    private fun generateSK(blockID: String?, commentID: String?) : String{
        return "$blockID#$commentID"
    }

    private fun createCommentObjectFromCommentRequest(commentRequest: CommentRequest?) : Comment? {
        return  commentRequest?.let {
            Comment(
                pk = "${commentRequest.nodeID}#COMMENT",
                sk = "${commentRequest.blockID}#${commentRequest.commentID}",
                commentBody = commentRequest.commentBody,
                commentedBy = commentRequest.commentedBy
            )
        }
    }
}
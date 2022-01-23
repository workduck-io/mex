package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.CommentRequest
import com.serverless.models.WDRequest
import com.workduck.models.Comment
import com.workduck.models.Identifier
import com.workduck.models.UserIdentifier
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

    fun getComment(entityID: String, commentID: String) : Comment? {
        val pk = generatePK(entityID)
        return commentRepository.getComment(pk, commentID)
    }

    fun createComment(commentRequest : WDRequest?) : Comment? {
        val comment = createCommentObjectFromCommentRequest(commentRequest as CommentRequest)
        return repository.create(comment)
    }

    fun updateComment(commentRequest: WDRequest)  {
        val comment = createCommentObjectFromCommentRequest(commentRequest as CommentRequest)
        commentRepository.updateComment(comment)
    }

    fun deleteComment(entityID: String, commentID : String) {
        val pk = generatePK(entityID)
        commentRepository.deleteComment(pk, commentID)
    }


    fun getAllComments(id: String) : List<Comment>{


        //val identifier = Identifier()
        return when {
            isNodeID(id) || isBlockID(id) -> commentRepository.getAllCommentsOfNodeOrBlock(generatePK(id))
            isUserID(id) -> commentRepository.getAllCommentsOfUser(id)
            else -> throw Exception("Invalid ID")
        }

    }

    private fun isUserID(id : String) = id.startsWith("USER")

    private fun isNodeID(id : String) = id.startsWith("NODE")

    private fun isBlockID(id : String) = id.startsWith("BLOCK")

    private fun generatePK(entityID: String) : String = "$entityID#COMMENT"

    private fun createCommentObjectFromCommentRequest(commentRequest: CommentRequest) : Comment {
        return Comment(
            pk = "${commentRequest.entityID}#COMMENT",
            sk = commentRequest.commentID,
            commentBody = commentRequest.commentBody,
            commentedBy = UserIdentifier(commentRequest.commentedBy)
        )
    }
}


fun main(){
    val jsonString: String = """
    {
        "type" : "CommentRequest",
        "entityID" : "NODE1",
        "commentedBy" : "USERVarun Garg",
        "commentBody": {
            "id": "sampleParentID",
            "elementType": "paragraph",
            "content" : "Comment Text"
        }
    }
    """


    val updateJsonString: String = """
    {
        "type" : "CommentRequest",
        "entityID" : "NODE1",
        "commentID" : "COMMENT1",
        "commentedBy" : "USERVarun Garg",
        "commentBody": {
            "id": "sampleParentID",
            "elementType": "paragraph",
            "content" : "Comment Text2"
        }
    }
    """

    val commentRequest = ObjectMapper().readValue<WDRequest>(jsonString)
    CommentService().updateComment(commentRequest)

}
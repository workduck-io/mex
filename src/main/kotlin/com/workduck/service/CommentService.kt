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

    fun getComment(nodeID: String?, blockID: String?, commentID: String?) : Comment? {
        val pk = generatePK(nodeID)
        val sk = generateSK(blockID, commentID)
        return commentRepository.getComment(pk, sk)
    }

    fun createComment(commentRequest : WDRequest?) : Comment? {
        val comment = createCommentObjectFromCommentRequest(commentRequest as CommentRequest)
        return repository.create(comment)
    }

    fun updateComment(commentRequest: WDRequest)  {
        val comment = createCommentObjectFromCommentRequest(commentRequest as CommentRequest)

        commentRepository.updateComment(comment)
    }

    fun deleteComment(nodeID: String?, blockID : String?, commentID : String?) {
        val pk = generatePK(nodeID)
        val sk = generateSK(blockID, commentID)

        commentRepository.deleteComment(pk, sk)

    }


    fun getAllComments(compositeIDList: List<String>) : List<Comment>{

        return when (compositeIDList.size) {
            1 -> commentRepository.getAllCommentsOfNode(generatePK(compositeIDList[0]))
            2 -> commentRepository.getAllCommentsOfBlock(generatePK(compositeIDList[0]), compositeIDList[1])
            else -> throw Exception("Invalid ID")
        }

    }


    private fun generatePK(nodeID: String?) : String{
        return "$nodeID#COMMENT"
    }

    private fun generateSK(blockID: String?, commentID: String?) : String{
        return "$blockID#$commentID"
    }

    private fun createCommentObjectFromCommentRequest(commentRequest: CommentRequest) : Comment {
        return Comment(
            pk = "${commentRequest.nodeID}#COMMENT",
            sk = "${commentRequest.blockID}#${commentRequest.commentID}",
            commentBody = commentRequest.commentBody,
            commentedBy = UserIdentifier(commentRequest.commentedBy)
        )
    }
}


fun main(){
    val jsonString: String = """
    {
        "type" : "CommentRequest",
        "nodeID" : "NODE1",
        "blockID": "BLOCK1",
        "commentID" : "COMMENT4C3RX7K98FD47Z10RJ0YTE8JJP",
        "commentedBy" : "Varun Garg",
        "commentBody": {
            "id": "sampleParentID",
            "elementType": "paragraph",
            "content" : "Comment Textt"
        }
    }
    """


    val updateJsonString: String = """
    {
        "type" : "CommentRequest",
        "nodeID" : "NODE1",
        "blockID": "BLOCK2",
        "commentID" : "COMMENT1",
        "commentedBy" : "Varun Garg",
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
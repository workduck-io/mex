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

    fun deleteComment(nodeID: String?, blockID : String?, commentID : String?) : String? {
        val pk = generatePK(nodeID)
        val sk = generateSK(blockID, commentID)

        return commentRepository.deleteComment(pk, sk)

    }


    fun getAllCommentsOfNode(nodeID: String?) : List<Comment>{
        val pk = generatePK(nodeID)
        return commentRepository.getAllCommentsOfNode(pk)
    }

    fun getAllCommentsOfBlock(nodeID: String?, blockID: String?) : List<Comment>{
        val pk = generatePK(nodeID)
        return commentRepository.getAllCommentsOfBlock(pk, blockID)
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


fun main(){
    val jsonString: String = """
    {
        "type" : "CommentRequest",
        "nodeID" : "NODE1",
        "blockID": "BLOCK1",
        "commentID" : "COMMENT1",
        "commentedBy" : "Varun Garg",
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

//    val commentRequest = ObjectMapper().readValue<CommentRequest>(jsonString)
//    CommentService().createComment(commentRequest)



    //val updateCommentRequest = ObjectMapper().readValue<WDRequest>(updateJsonString)
    //CommentService().updateComment(updateCommentRequest)



    //println(CommentService().getComment("NODE1", "BLOCK2", "COMMENT1"))


    //println(Helper.objectMapper.writeValueAsString(CommentService().getAllCommentsOfNode("NODE1")))

    println(Helper.objectMapper.writeValueAsString(CommentService().getAllCommentsOfBlock("NODE1", "BLOCK1")))


}
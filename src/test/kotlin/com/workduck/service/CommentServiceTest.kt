package com.workduck.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.WDRequest
import com.workduck.models.Comment
import com.workduck.repositories.CommentRepository
import com.workduck.repositories.Repository
import com.workduck.utils.Helper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Nested

internal class CommentServiceTest {

    private val repository = mockk<Repository<Comment>>()
    private val commentRepository = mockk<CommentRepository>()

    private val commentService = CommentService(repository = repository, commentRepository = commentRepository)

    @Test
    fun getComment() {
        every { commentRepository.getComment(any(),any()) } returns Comment(pk = "NODE1#COMMENT", sk = "COMMENT1")
        val result = commentService.getComment("NODE1", "COMMENT1")

        verify { commentRepository.getComment("NODE1#COMMENT", "COMMENT1") }
        assertThat("NODE1#COMMENT").isEqualTo(result?.pk)
    }

    @Test
    fun createComment() {
        every{ repository.create(any()) } returns Comment(pk = "NODE1#COMMENT", sk = "COMMENT1")

        val json = """
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

        val request = Helper.objectMapper.readValue<WDRequest>(json)
        val result  = commentService.createComment(request)
        assertThat("NODE1#COMMENT").isEqualTo(result?.pk)
    }

    @Test
    fun updateComment() {
        every{ commentRepository.updateComment(any()) } returns Unit

        val json = """
            {
            "type" : "CommentRequest",
            "entityID" : "NODE1",
            "commentID": "COMMENT1",
            "commentedBy" : "USERVarun Garg",
            "commentBody": {
                "id": "sampleParentID",
                "elementType": "paragraph",
                "content" : "Comment Text"
            }
        }
        """

        val request = Helper.objectMapper.readValue<WDRequest>(json)
        commentService.updateComment(request)
        verify { commentRepository.updateComment(any()) }
    }

    @Test
    fun deleteComment() {
        every{ commentRepository.deleteComment(any(), any()) } returns Unit
        commentService.deleteComment("NODE1", "COMMENT1")
        verify { commentRepository.deleteComment("NODE1#COMMENT", "COMMENT1") }
    }

    @Nested
    inner class GetAllComments{
        @Test
        fun `no valid entity id`() {
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                    .isThrownBy { commentService.getAllComments("RandomPrefix") }
        }

        @Test
        fun `user id provided`() {
            every{ commentRepository.getAllCommentsOfUser(any()) } returns listOf()

            commentService.getAllComments("USER1")
            verify(exactly = 1) { commentRepository.getAllCommentsOfUser(any()) }
            verify(exactly = 0) { commentRepository.getAllCommentsOfNodeOrBlock(any()) }
        }
    }

}
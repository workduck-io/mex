package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.CloneSnippetRequest
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.SnippetRequest
import com.serverless.models.requests.UpdateSnippetVersionRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.createSnippetObjectFromSnippetRequest
import com.serverless.utils.createSnippetObjectFromUpdateSnippetVersionRequest
import com.serverless.utils.setVersion
import com.workduck.models.Entity
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.Snippet
import com.workduck.models.SnippetIdentifier
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.PageRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.SnippetRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.PageHelper.comparePageWithStoredPage
import com.workduck.utils.PageHelper.convertGenericRequestToList
import com.workduck.utils.PageHelper.createDataOrderForPage
import com.workduck.utils.PageHelper.mergePageVersions
import com.workduck.utils.SnippetHelper.getSnippetSK
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class SnippetService {
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

    private val snippetRepository = SnippetRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName)
    private val pageRepository: PageRepository<Snippet> = PageRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName)
    private val repository: Repository<Snippet> = RepositoryImpl(dynamoDB, mapper, pageRepository, dynamoDBMapperConfig)

    fun createSnippet(wdRequest: WDRequest, userEmail: String, workspaceID: String): Entity {
        val snippet: Snippet = (wdRequest as SnippetRequest).createSnippetObjectFromSnippetRequest(userEmail, workspaceID)
        return createSnippetWithVersion(snippet)
    }

    private fun createSnippetWithVersion(snippet: Snippet, version: Long = 1) : Entity{
        snippet.dataOrder = createDataOrderForPage(snippet)

        snippet.setVersion(version)

        /* only when node is actually being created */
        snippet.createdBy = snippet.lastEditedBy

        for (e in snippet.data!!) {
            e.createdBy = snippet.lastEditedBy
            e.lastEditedBy = snippet.lastEditedBy
            e.createdAt = snippet.createdAt
            e.updatedAt = snippet.createdAt
        }

        LOG.info("Creating node : $snippet")

        return repository.create(snippet)
    }

    fun getSnippet(snippetID: String, workspaceID: String, version: Long? = null) : Entity {
        return when(version){
            null -> getLatestSnippet(snippetID, workspaceID)
            else -> getSnippetByVersion(snippetID, workspaceID, version)
        }
    }

    fun getAllVersionsOfSnippet(snippetID: String, workspaceID: String) : List<Entity> {
        return snippetRepository.getAllVersionsOfSnippet(snippetID, workspaceID)
    }

    private fun getLatestSnippet(snippetID: String, workspaceID: String) : Entity {
        return getSnippetByVersion(snippetID, workspaceID, getLatestVersionNumberOfSnippet(snippetID, workspaceID))
    }


    fun getSnippetByVersion(snippetID: String, workspaceID: String,  version: Long) : Entity {
        return snippetRepository.getSnippetByVersion(snippetID, workspaceID, version)
    }


    /* given a snippet version, update snippet info and keep the version number same */
    fun updateSnippet(wdRequest: WDRequest, userEmail: String, workspaceID: String) : Entity {
        val updateRequest = wdRequest as UpdateSnippetVersionRequest
        val snippet: Snippet = updateRequest.createSnippetObjectFromUpdateSnippetVersionRequest(userEmail, workspaceID, updateRequest.version)

        /* since null fields won't be edited in DDB */
        Snippet.setCreatedFieldsNull(snippet)

        snippet.dataOrder = createDataOrderForPage(snippet)

        LOG.info("Updating node : $snippet")
        return repository.update(snippet)

    }

    fun createNextSnippetVersion(wdRequest: WDRequest, userEmail: String, workspaceID: String) : Entity {
        val snippet: Snippet = (wdRequest as SnippetRequest).createSnippetObjectFromSnippetRequest(userEmail, workspaceID)
        val latestSnippetVersion = getLatestVersionNumberOfSnippet(snippet.id, workspaceID)
        return createSnippetWithVersion(snippet, latestSnippetVersion + 1)
    }

    private fun getLatestVersionNumberOfSnippet(snippetID: String, workspaceID: String) : Long{
        return snippetRepository.getLatestVersionNumberOfSnippet(snippetID, workspaceID)
    }


    fun getSnippet(snippetID: String, workspaceID: String): Entity? {
        return repository.get(WorkspaceIdentifier(workspaceID), SnippetIdentifier(snippetID), Snippet::class.java)
    }

    fun makeSnippetPublic(snippetID: String, workspaceID: String, version: Long) {
        togglePublicAccess(snippetID, workspaceID, version, 1)
    }

    fun makeSnippetPrivate(snippetID: String, workspaceID: String, version: Long) {
        togglePublicAccess(snippetID, workspaceID, version, 0)
    }

    private fun togglePublicAccess(snippetID: String, workspaceID: String, version: Long, accessValue: Long){
        when(version == -1L ){
            true -> {
                val latestVersion = snippetRepository.getLatestVersionNumberOfSnippet(snippetID, workspaceID)
                pageRepository.togglePagePublicAccess(getSnippetSK(snippetID, latestVersion), workspaceID, accessValue)
            }
            else -> pageRepository.togglePagePublicAccess(getSnippetSK(snippetID, version), workspaceID,  accessValue)
        }
    }

    fun getPublicSnippet(snippetID: String, version: Long): Entity? {
        return pageRepository.getPublicPage(getSnippetSK(snippetID, version), Snippet::class.java)
    }


    fun archiveSnippets(wdRequest: WDRequest, workspaceID: String): MutableList<String> {
        val snippetIDList = convertGenericRequestToList(wdRequest as GenericListRequest)
        LOG.info(snippetIDList)
        return pageRepository.unarchiveOrArchivePages(snippetIDList, workspaceID, ItemStatus.ARCHIVED)
    }

    fun unarchiveSnippets(wdRequest: WDRequest, workspaceID: String): MutableList<String> {
        val snippetIDList = convertGenericRequestToList(wdRequest as GenericListRequest)
        return pageRepository.unarchiveOrArchivePages(snippetIDList, workspaceID, ItemStatus.ACTIVE)
    }

    fun deleteArchivedSnippets(wdRequest: WDRequest, workspaceID: String): MutableList<String> {
        val snippetIDList = convertGenericRequestToList(wdRequest as GenericListRequest)
        val deletedSnippetsList: MutableList<String> = mutableListOf()
        require(getAllArchivedSnippetIDsOfWorkspace(workspaceID).sorted() == snippetIDList.sorted()) { "The passed IDs should be present and archived" }
        for (snippetID in snippetIDList) {
            repository.delete(WorkspaceIdentifier(workspaceID), SnippetIdentifier(snippetID))?.also {
                deletedSnippetsList.add(it.id)
            }
        }
        return deletedSnippetsList
    }

    fun getAllArchivedSnippetIDsOfWorkspace(workspaceID: String): List<String> {
        return pageRepository.getAllArchivedPagesOfWorkspace(workspaceID, ItemType.Snippet)
    }


    fun clonePublicSnippet(wdRequest: WDRequest, userEmail: String, workspaceID: String): Entity {
        val cloneRequest = wdRequest as CloneSnippetRequest
        val publicSnippet = getPublicSnippet(cloneRequest.snippetID, cloneRequest.version) as Snippet?
        require(publicSnippet != null) {"Invalid Public Snippet ID passed"}

        val newSnippet = createSnippetFromPublicSnippet(publicSnippet, userEmail, workspaceID)
        return createSnippetWithVersion(newSnippet)

    }

    private fun createSnippetFromPublicSnippet(publicSnippet: Snippet, userEmail: String, workspaceID: String): Snippet{
        return Snippet(
                workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                lastEditedBy = userEmail,
                data = publicSnippet.data,
                referenceSnippet = SnippetIdentifier(publicSnippet.id)
        )
    }

    companion object {
        private val LOG = LogManager.getLogger(SnippetService::class.java)
    }
}

fun main(){

    val jsonString: String = """
 {
     "type" : "SnippetRequest",
     "lastEditedBy" : "USERVarun",
     "id": "SNIPPET1",
     "title" : "Random Heading 2",
     "data": [
     {
         "id": "sampleParentID",
         "elementType": "paragraph",
         "children": [
         {
             "id" : "sampleChildID",
             "content" : "sample child content 2",
             "elementType": "paragraph",
             "properties" :  { "bold" : true, "italic" : true  }
         }
         ]
     },
     {
         "id": "1234",
         "elementType": "paragraph",
         "children": [
         {
             "id" : "sampleChildID",
             "content" : "sample child content",
             "elementType": "paragraph",
             "properties" :  { "bold" : true, "italic" : true  }
         }
         ]
     }
     ]
 }
"""

    val snippetRequest = Helper.objectMapper.readValue<WDRequest>(jsonString)
    SnippetService().createNextSnippetVersion(snippetRequest, "varuntest@workduck.io", "WORKSPACE1")

}
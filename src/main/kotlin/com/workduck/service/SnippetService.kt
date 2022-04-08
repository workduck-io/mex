package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.CloneSnippetRequest
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.SnippetRequest
import com.serverless.models.requests.WDRequest
import com.workduck.models.Entity
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.Page
import com.workduck.models.Snippet
import com.workduck.models.SnippetIdentifier
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.PageRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.PageHelper.comparePageWithStoredPage
import com.workduck.utils.PageHelper.convertGenericRequestToList
import com.workduck.utils.PageHelper.createDataOrderForPage
import com.workduck.utils.PageHelper.mergePageVersions
import kotlinx.coroutines.launch
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

    private val pageRepository: PageRepository<Snippet> = PageRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName)
    private val repository: Repository<Snippet> = RepositoryImpl(dynamoDB, mapper, pageRepository, dynamoDBMapperConfig)

    fun createAndUpdateSnippet(wdRequest: WDRequest, userEmail: String, workspaceID: String): Entity? {
        val snippet: Snippet = createSnippetObjectFromSnippetRequest(wdRequest as SnippetRequest, userEmail, workspaceID)
        val storedSnippet = getSnippet(snippet.id, workspaceID) as Snippet?

        return if (storedSnippet == null) {
            createSnippet(snippet)
        } else {
            updateSnippet(snippet, storedSnippet)
        }
    }

    private fun createSnippet(snippet: Snippet): Entity {
        snippet.dataOrder = createDataOrderForPage(snippet)

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

    private fun updateSnippet(snippet: Snippet, storedSnippet: Snippet): Entity? {
        Page.populatePageWithCreatedFields(snippet, storedSnippet)

        snippet.dataOrder = createDataOrderForPage(snippet)

        /* to update block level details for accountability */
        val nodeChanged: Boolean = comparePageWithStoredPage(snippet, storedSnippet)

        if (!nodeChanged) {
            return storedSnippet
        }

        /* to make the locking versions same */
        mergePageVersions(snippet, storedSnippet)

        LOG.info("Updating node : $snippet")
        return repository.update(snippet)
    }

    fun getSnippet(snippetID: String, workspaceID: String): Entity? {
        return repository.get(WorkspaceIdentifier(workspaceID), SnippetIdentifier(snippetID), Snippet::class.java)
    }

    fun makeSnippetPublic(snippetID: String, workspaceID: String) = runBlocking {
        launch {pageRepository.togglePagePublicAccess(snippetID, workspaceID,  1) }
    }

    fun makeSnippetPrivate(snippetID: String, workspaceID: String) {
        pageRepository.togglePagePublicAccess(snippetID, workspaceID, 0)
    }

    fun getPublicSnippet(snippetID: String, workspaceID: String): Entity? {
        return pageRepository.getPublicPage(snippetID, workspaceID, Snippet::class.java)
    }

    private fun createSnippetObjectFromSnippetRequest(snippetRequest: SnippetRequest, userEmail: String, workspaceID: String): Snippet {
        return Snippet(
            id = snippetRequest.id,
            workspaceIdentifier = WorkspaceIdentifier(workspaceID),
            lastEditedBy = userEmail,
            data = snippetRequest.data
        )
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
        val publicSnippet = getPublicSnippet(cloneRequest.snippetID, workspaceID) as Snippet?
        require(publicSnippet != null) {"Invalid Public Snippet ID passed"}

        val newSnippet = createSnippetFromPublicSnippet(publicSnippet, userEmail, workspaceID)
        return createSnippet(newSnippet)

    }

    private fun createSnippetFromPublicSnippet(publicSnippet: Snippet, userEmail: String, workspaceID: String): Snippet{
        return Snippet(
                workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                lastEditedBy = userEmail,
                data = publicSnippet.data
        )
    }

    companion object {
        private val LOG = LogManager.getLogger(SnippetService::class.java)
    }
}
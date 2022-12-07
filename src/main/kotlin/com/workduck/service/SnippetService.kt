package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.serverless.models.requests.MetadataRequest
import com.serverless.models.requests.SnippetRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.createSnippetObjectFromSnippetRequest
import com.serverless.utils.setVersion
import com.workduck.models.Entity
import com.workduck.models.Snippet
import com.workduck.models.SnippetIdentifier
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.PageRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.SnippetRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.PageHelper.createDataOrderForPage
import com.workduck.utils.SnippetHelper.getSnippetSK
import com.workduck.utils.SnippetHelper.isCreatePossible
import com.workduck.utils.extensions.orderPage
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

    fun createAndUpdateSnippet(wdRequest: WDRequest, userID: String, workspaceID: String, createNextVersion: Boolean = false): Entity {
        val snippet: Snippet = (wdRequest as SnippetRequest).createSnippetObjectFromSnippetRequest(userID, workspaceID)
        setMetadata(snippet)
        return if(!createNextVersion) { /* either create a new snippet or update the existing version */
            if (isCreatePossible(snippet.version!!)) { /* update and create both allowed */
                createOrUpdate(snippet)
            } else { /* only update allowed */
                updateSnippet(snippet)
            }
        } else{
            createNextVersion(snippet)
        }

    }


    private fun createNextVersion(snippet: Snippet) : Entity{
        require(getLatestVersionNumberOfSnippet(snippet.id, snippet.workspaceIdentifier.id) == snippet.version ) {
            "To create next version, pass the latest existing version number"
        }
        snippet.setVersion(snippet.version!! + 1)
        return snippetRepository.createSnippet(snippet)
    }

    private fun createOrUpdate(snippet: Snippet) : Entity {
        return try{
            snippetRepository.createSnippet(snippet)
        } catch (e : ConditionalCheckFailedException){
            updateSnippet(snippet)
        }
    }


    private fun updateSnippet(snippet: Snippet) : Entity{
        Snippet.setCreatedFieldsNull(snippet)
        return snippetRepository.updateSnippet(snippet)
    }


    private fun setMetadata(snippet: Snippet){
        snippet.dataOrder = createDataOrderForPage(snippet)

        snippet.createdBy = snippet.lastEditedBy /* if an update operation is expected, this field will be set to null */

        if(snippet.data.isNullOrEmpty()) return
        for (e in snippet.data!!) {
            e.createdBy = snippet.lastEditedBy
            e.lastEditedBy = snippet.lastEditedBy
            e.createdAt = snippet.createdAt
            e.updatedAt = snippet.createdAt
        }

    }


    private fun createSnippetWithVersion(snippet: Snippet, version: Int = 1){
        setMetadata(snippet)
        snippet.setVersion(version)
        repository.create(snippet)
    }

    fun getSnippet(snippetID: String, workspaceID: String, version: Int? = null) : Snippet? {
        return when(version){
            null -> getLatestSnippet(snippetID, workspaceID)
            else -> getSnippetByVersion(snippetID, workspaceID, version)
        }?.let {
            snippet -> snippet.orderPage() as Snippet
        }
    }

    fun getAllVersionsOfSnippet(snippetID: String, workspaceID: String) : List<Int?> {
        return snippetRepository.getAllVersionsOfSnippet(snippetID, workspaceID)
    }

    private fun getLatestSnippet(snippetID: String, workspaceID: String) : Snippet? {
        return getSnippetByVersion(snippetID, workspaceID, getLatestVersionNumberOfSnippet(snippetID, workspaceID))
    }


    fun getSnippetByVersion(snippetID: String, workspaceID: String,  version: Int) : Snippet? {
        return snippetRepository.getSnippetByVersion(snippetID, workspaceID, version)
    }

    private fun getLatestVersionNumberOfSnippet(snippetID: String, workspaceID: String) : Int{
        return snippetRepository.getLatestVersionNumberOfSnippet(snippetID, workspaceID)
    }


    fun makeSnippetPublic(snippetID: String, workspaceID: String, version: Int) {
        togglePublicAccess(snippetID, workspaceID, version, 1)
    }

    fun makeSnippetPrivate(snippetID: String, workspaceID: String, version: Int) {
        togglePublicAccess(snippetID, workspaceID, version, 0)
    }

    private fun togglePublicAccess(snippetID: String, workspaceID: String, version: Int, accessValue: Int){
        when(version == -1 ){
            true -> {
                val latestVersion = snippetRepository.getLatestVersionNumberOfSnippet(snippetID, workspaceID)
                pageRepository.togglePagePublicAccess(getSnippetSK(snippetID, latestVersion), workspaceID, accessValue)
            }
            else -> pageRepository.togglePagePublicAccess(getSnippetSK(snippetID, version), workspaceID,  accessValue)
        }
    }

    fun getPublicSnippet(snippetID: String, version: Int): Entity? {
        return pageRepository.getPublicPage(getSnippetSK(snippetID, version), Snippet::class.java)
    }


    fun deleteSnippet(snippetID: String, version: Int? = null, workspaceID: String) {
        val versionToDelete = when(version != null){
            true -> version
            false -> getLatestVersionNumberOfSnippet(snippetID, workspaceID)
        }
        snippetRepository.deleteSnippetByVersion(snippetID, workspaceID, versionToDelete)
    }

    fun deleteAllVersionsOfSnippet(snippetID: String, workspaceID: String){
        val listOfSnippets = createSnippetsWithPKSK(getAllVersionsOfSnippet(snippetID, workspaceID), snippetID, workspaceID)
        snippetRepository.batchDeleteVersions(listOfSnippets)
    }

    private fun createSnippetsWithPKSK(list : List<Int?>,snippetID: String,  workspaceID: String) : List<Snippet> {
        return list.map{
            Snippet(
                    id = snippetID,
                    workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                    version = it
            )
        }
    }

    fun clonePublicSnippet(snippetID: String, version: Int, userID: String, workspaceID: String): String {
        val publicSnippet = getPublicSnippet(snippetID, version) as Snippet? ?: throw NoSuchElementException("Requested Snippet Not Found")
        val newSnippet = createSnippetFromPublicSnippet(publicSnippet, userID, workspaceID)
        createSnippetWithVersion(newSnippet)
        return newSnippet.id

    }

    fun getAllSnippetsMetadataOfWorkspace(workspaceID: String) : List<Map<String, String>> {
        return snippetRepository.getAllSnippetsMetadataOfWorkspace(workspaceID)
    }

    fun getAllSnippetsDataOfWorkspace(workspaceID: String) : List<Snippet> {
        return snippetRepository.getAllSnippetsDataOfWorkspace(workspaceID)
    }



    private fun createSnippetFromPublicSnippet(publicSnippet: Snippet, userID: String, workspaceID: String): Snippet{
        return Snippet(
                workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                lastEditedBy = userID,
                data = publicSnippet.data,
                referenceSnippet = SnippetIdentifier(publicSnippet.id),
                title = publicSnippet.title
        )
    }

    fun updateMetadataOfNode(wdRequest: WDRequest, snippetID: String, userWorkspaceID: String, userID: String){
        val metadata = (wdRequest as MetadataRequest).pageMetadata
        pageRepository.updateMetadataOfPage(snippetID, userWorkspaceID, metadata, userID)
    }

    companion object {
        private val LOG = LogManager.getLogger(SnippetService::class.java)
    }
}
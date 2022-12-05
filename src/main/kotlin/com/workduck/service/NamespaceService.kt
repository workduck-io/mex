package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.serverless.models.requests.NamespaceRequest
import com.serverless.models.requests.SharedNamespaceRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AccessType

import com.workduck.models.EntityOperationType
import com.workduck.models.HierarchyUpdateSource
import com.workduck.models.ItemType
import com.workduck.models.Namespace
import com.workduck.models.NamespaceAccess
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.WorkspaceIdentifier

import com.workduck.repositories.NamespaceRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.AccessItemHelper
import com.workduck.utils.DDBHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import com.workduck.utils.extensions.toNamespace
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager

class NamespaceService (


    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
    private val dynamoDB: DynamoDB = DynamoDB(client),
    private val mapper: DynamoDBMapper = DynamoDBMapper(client),

    private val tableName: String = DDBHelper.getTableName(),

    private val dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

    private val namespaceRepository: NamespaceRepository = NamespaceRepository(dynamoDB, mapper, dynamoDBMapperConfig),
    private val repository: Repository<Namespace> = RepositoryImpl(dynamoDB, mapper, namespaceRepository, dynamoDBMapperConfig),

    private val nodeService : NodeService = NodeService(),
    val namespaceAccessService: NamespaceAccessService = NamespaceAccessService(namespaceRepository)
) {

    fun createNamespace(namespaceRequest: WDRequest, workspaceID: String, userID: String) {
        val namespace: Namespace = (namespaceRequest as NamespaceRequest).toNamespace(workspaceID, userID)
        require(!checkIfNamespaceNameExists(workspaceID, namespace.name)) { "Cannot use an existing Namespace Name" }
        repository.create(namespace)
    }

    fun getNamespace(workspaceID: String, namespaceID: String, userID: String): Namespace? {
        require(namespaceAccessService.checkIfUserHasAccess(workspaceID, namespaceID, userID, EntityOperationType.READ)) { Messages.ERROR_NAMESPACE_PERMISSION }
        return getNamespaceAfterPermissionCheck(namespaceID)
    }

    fun getNamespaceAfterPermissionCheck(namespaceID: String) : Namespace? {
        return namespaceRepository.getNamespaceByNamespaceID(namespaceID)
    }


    fun updateNamespace(namespaceRequest: WDRequest, userWorkspaceID: String, userID: String) {
        val namespace = (namespaceRequest as NamespaceRequest).toNamespace(userWorkspaceID, null)
        val workspaceIDOfNamespace = namespaceAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(namespace.id, userWorkspaceID, userID, EntityOperationType.WRITE)[Constants.WORKSPACE_ID]!!
        namespaceRepository.updateNamespace(workspaceIDOfNamespace, namespace.id, namespace)
    }

    fun updateNamespace(namespace: Namespace) {
        repository.update(namespace)
    }

    fun deleteNamespace(namespaceID: String, userWorkspaceID: String, userID: String) = runBlocking {

        val workspaceIDOfNamespace = namespaceAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(namespaceID, userWorkspaceID, userID, EntityOperationType.MANAGE)[Constants.WORKSPACE_ID]
                ?: throw IllegalArgumentException("Invalid Parameters")

        val jobToGetListOfNodeIDsToDelete = async { nodeService.getAllNodesWithNamespaceID(namespaceID, workspaceIDOfNamespace) }
        launch { nodeService.batchDeleteNodes(jobToGetListOfNodeIDsToDelete.await(), workspaceIDOfNamespace) }
        launch { repository.delete(WorkspaceIdentifier(workspaceIDOfNamespace), NamespaceIdentifier(namespaceID)) }


    }


    fun makeNamespacePublic(namespaceID: String, userWorkspaceID: String, userID: String) {
        val namespaceWorkspaceID = namespaceAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(namespaceID, userWorkspaceID, userID, EntityOperationType.MANAGE)[Constants.WORKSPACE_ID] ?: throw IllegalArgumentException(Messages.ERROR_GETTING_WORKSPACE)
        require( !namespaceRepository.isNamespacePublic(namespaceID, namespaceWorkspaceID) ) { Messages.ERROR_NAMESPACE_PUBLIC }
        val nodeIDList = nodeService.getAllNodesWithNamespaceIDAndAccess(namespaceID, namespaceWorkspaceID, 0) /* get all private nodes */
        nodeService.makeNodesPublicOrPrivateInParallel(nodeIDList, namespaceWorkspaceID, 1)
        namespaceRepository.setPublicAccessValue(namespaceID, namespaceWorkspaceID, 1)

    }

    fun makeNamespacePrivate(namespaceID: String, userWorkspaceID: String, userID: String) {
        val namespaceWorkspaceID = namespaceAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(namespaceID, userWorkspaceID, userID, EntityOperationType.MANAGE)[Constants.WORKSPACE_ID] ?: throw IllegalArgumentException(Messages.ERROR_GETTING_WORKSPACE)
        require( namespaceRepository.isNamespacePublic(namespaceID, namespaceWorkspaceID) ) { Messages.ERROR_NAMESPACE_PRIVATE }
        val nodeIDList = nodeService.getAllNodesWithNamespaceIDAndAccess(namespaceID, namespaceWorkspaceID, 1) /* get all public nodes */
        nodeService.makeNodesPublicOrPrivateInParallel(nodeIDList, namespaceWorkspaceID, 0)
        namespaceRepository.setPublicAccessValue(namespaceID, namespaceWorkspaceID, 0)

    }

    fun getAllNamespaceData(workspaceID: String): List<Namespace> {
        return namespaceRepository.getAllNamespaceData(workspaceID)
    }


    private fun checkIfNamespaceNameExists(workspaceID: String, namespaceName: String) : Boolean {
        return namespaceRepository.checkIfNamespaceNameExists(workspaceID, namespaceName)
    }


    fun getPublicNamespace(namespaceID: String): Namespace {
        return namespaceRepository.getPublicNamespace(namespaceID)
    }

    fun addPathToHierarchy(workspaceID: String, namespaceID: String, path: String) {
        namespaceRepository.addNodePathToHierarchy(workspaceID, namespaceID, path)
    }

    fun updateNamespaceHierarchy(
            namespace: Namespace,
            newNodeHierarchy: List<String>,
            hierarchyUpdateSource: HierarchyUpdateSource
    ) {
        Namespace.populateHierarchiesAndUpdatedAt(namespace, newNodeHierarchy, namespace.archivedNodeHierarchyInformation)
        namespace.hierarchyUpdateSource = hierarchyUpdateSource
        updateNamespace(namespace)
    }

    fun getNodeHierarchyOfWorkspaceWithMetaData(workspaceID: String): Map<String, Any> = runBlocking {
        val jobToGetHierarchy =  async { getNodeHierarchyOfWorkspace(workspaceID) }
        val jobToGetNodesMetadata = async { nodeService.getMetadataForNodesOfWorkspace(workspaceID) }
        return@runBlocking mapOf("hierarchy" to jobToGetHierarchy.await(), "nodesMetadata" to jobToGetNodesMetadata.await())
    }


    fun getNodeHierarchyOfWorkspace(workspaceID: String): Map<String, Any>  = runBlocking {
        val jobToGetNamespaces = async { getAllNamespaceData(workspaceID) }

        val hierarchyMap: MutableMap<String, Any> = mutableMapOf()

        hierarchyMap[Constants.NAMESPACE_INFO] = constructNamespaceInfo(jobToGetNamespaces.await())

        return@runBlocking hierarchyMap
    }

    private fun constructNamespaceInfo(namespaceList: List<Namespace>) : MutableMap<String, Any>{

        val namespaceHierarchyJson: MutableMap<String, Any> = mutableMapOf()
        for (namespace in namespaceList) {
            val mapOfNamespaceData = mutableMapOf<String, Any?>()
            mapOfNamespaceData[Constants.NAME] = namespace.name
            mapOfNamespaceData[Constants.NODE_HIERARCHY] = namespace.nodeHierarchyInformation
            mapOfNamespaceData[Constants.NAMESPACE_METADATA] = namespace.namespaceMetadata
            namespaceHierarchyJson.putIfAbsent(namespace.id, mapOfNamespaceData)
        }
        return namespaceHierarchyJson

    }


    fun shareNamespace(wdRequest: WDRequest, granterID: String, granterWorkspaceID: String) {
        val sharedNamespaceRequest = wdRequest as SharedNamespaceRequest
        val userIDs = AccessItemHelper.getUserIDsWithoutGranterID(sharedNamespaceRequest.userIDs, granterID) // remove granterID from userIDs if applicable.

        if (userIDs.isEmpty()) return

        val workspaceDetailsOfNamespace = namespaceAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(sharedNamespaceRequest.namespaceID, granterWorkspaceID, granterID, EntityOperationType.MANAGE)
        val namespaceAccessItems = AccessItemHelper.getNamespaceAccessItems(sharedNamespaceRequest.namespaceID, workspaceDetailsOfNamespace[Constants.WORKSPACE_ID]!!, granterID, userIDs, sharedNamespaceRequest.accessType)
        namespaceRepository.createBatchNamespaceAccessItem(namespaceAccessItems)
    }


    fun revokeSharedAccess(wdRequest: WDRequest, revokerUserID: String, workspaceID: String) {
        val shareNamespaceRequest = wdRequest as SharedNamespaceRequest
        if (!namespaceAccessService.checkIfUserHasAccess(workspaceID, shareNamespaceRequest.namespaceID, revokerUserID, EntityOperationType.MANAGE)) throw NoSuchElementException("Namespace you're trying to share does not exist")

        // since PK and SK matter here for deletion, can fill dummy fields.
        val namespaceAccessItems = AccessItemHelper.getNamespaceAccessItems(shareNamespaceRequest.namespaceID, workspaceID, revokerUserID, shareNamespaceRequest.userIDs, shareNamespaceRequest.accessType)
        namespaceRepository.deleteBatchNamespaceAccessItems(namespaceAccessItems)
    }


    fun getAllNamespaceMetadata(workspaceID: String, userID: String): List<Map<String, String?>> = runBlocking {
        val jobToGetListOfNamespacesInOwnWorkspace = async { namespaceRepository.getAllNamespaceIDsForWorkspace(workspaceID) }

        val jobToGetAccessItemsForSharedNamespaces = async {  namespaceRepository.getAllSharedNamespacesWithUser(userID) }


        val setOfNamespaceIDToWorkspaceID = mutableSetOf<Pair<String, String>>()
        jobToGetListOfNamespacesInOwnWorkspace.await().map {
            setOfNamespaceIDToWorkspaceID.add(Pair(it, workspaceID))
        }

        return@runBlocking getNamespaceTitleWithIDs(jobToGetAccessItemsForSharedNamespaces.await(), setOfNamespaceIDToWorkspaceID)

    }

    fun getAllNamespacesOfWorkspace(workspaceID: String): List<Map<String, String?>> {
        val namespaceIDsOfWorkspace = namespaceRepository.getAllNamespaceIDsForWorkspace(workspaceID)

        val setOfNamespaceIDToWorkspaceID = mutableSetOf<Pair<String, String>>()

        namespaceIDsOfWorkspace.map {
            setOfNamespaceIDToWorkspaceID.add(Pair(it, workspaceID))
        }

        return getNamespaceTitleWithIDs(emptyMap(), setOfNamespaceIDToWorkspaceID)

    }

    fun getAllSharedNamespacesWithUser(userID: String): List<Map<String, String?>> {
        val accessItemsMapForSharedNamespaces = namespaceRepository.getAllSharedNamespacesWithUser(userID)
        return getNamespaceTitleWithIDs(accessItemsMapForSharedNamespaces, mutableSetOf())
    }


    fun getNamespaceTitleWithIDs(namespaceAccessItemsMap: Map<String, NamespaceAccess>, setOfNamespaceIDWorkspaceID: MutableSet<Pair<String, String>>): List<Map<String, String?>> {
        setOfNamespaceIDWorkspaceID.addAll(createSetFromNamespaceAccessItems(namespaceAccessItemsMap.values.toList()))

        val unprocessedData = namespaceRepository.batchGetNamespaceMetadataAndTitle(setOfNamespaceIDWorkspaceID)
        val listOfNamespaceData = mutableListOf<Map<String, String?>>()

        for (namespaceData in unprocessedData) {
            populateMapForSharedNamespaceData(namespaceData, namespaceAccessItemsMap).let {
                if(it.isNotEmpty()) listOfNamespaceData.add(it)
            }
        }

        return listOfNamespaceData
    }

    private fun populateMapForSharedNamespaceData(namespaceData: MutableMap<String, AttributeValue>, namespaceAccessItemsMap: Map<String, NamespaceAccess>): Map<String, String?> {
        val map = mutableMapOf<String, String?>()

        val namespaceID = namespaceData["SK"]!!.s
        map["namespaceID"] = namespaceID
        map["namespaceTitle"] = namespaceData["namespaceName"]!!.s
        map["accessType"] = namespaceAccessItemsMap[namespaceID]?.accessType?.name ?: AccessType.MANAGE.name
        map["granterID"] = namespaceAccessItemsMap[namespaceID]?.granterID



        val metadata  = if(namespaceData.containsKey("metadata")) namespaceData["metadata"]!!.s else null
        val namespaceMetadataJson = """
            {
                "createdAt" : ${namespaceData["createdAt"]!!.n} ,
                "updatedAt" : ${namespaceData["updatedAt"]!!.n} ,
                "metadata" : $metadata
                
            }
        """.trimIndent()
        map["namespaceMetadata"] = namespaceMetadataJson

        return map
    }


    private fun createSetFromNamespaceAccessItems(namespaceAccessItems: List<NamespaceAccess>): Set<Pair<String, String>> {
        return namespaceAccessItems.map { Pair(it.namespace.id, it.workspace.id) }.toSet()
    }


    fun getAllSharedUsersOfNamespace(workspaceID: String, namespaceID: String, userID: String): Map<String, String> = runBlocking {
        require(namespaceAccessService.checkIfUserHasAccess(workspaceID, namespaceID, userID, EntityOperationType.MANAGE)) { Messages.ERROR_NAMESPACE_PERMISSION }
        val jobToGetInvitedUsers = async { namespaceRepository.getSharedUserInformation(namespaceID) }
        val jobToGetNamespaceOwnerDetails = async { namespaceRepository.getOwnerDetailsFromNamespaceID(namespaceID) }

        val mapOfSharedUserDetails = jobToGetInvitedUsers.await().toMutableMap().also {
            it.putAll(jobToGetNamespaceOwnerDetails.await())
        }

        return@runBlocking mapOfSharedUserDetails
    }


    fun getAccessDataForUser(namespaceID: String, userID: String, userWorkspaceID: String): String {
        val workspaceIDOfNamespace : String? = namespaceRepository.getWorkspaceIDOfNamespace(namespaceID)
        require(!workspaceIDOfNamespace.isNullOrEmpty()) { Messages.INVALID_NAMESPACE_ID }

        if (workspaceIDOfNamespace == userWorkspaceID) return AccessType.OWNER.name

        return namespaceAccessService.getUserNamespaceAccessType(namespaceID, userID).name
    }



    companion object {
        private val LOG = LogManager.getLogger(NamespaceService::class.java)
    }
}

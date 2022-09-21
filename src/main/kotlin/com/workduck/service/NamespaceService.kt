package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.NamespaceRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants

import com.workduck.models.Entity
import com.workduck.models.HierarchyUpdateSource
import com.workduck.models.Identifier
import com.workduck.models.Namespace
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.WorkspaceIdentifier

import com.workduck.repositories.NamespaceRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import com.workduck.utils.extensions.toNamespace
import org.apache.logging.log4j.LogManager

class NamespaceService (


    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
    private val dynamoDB: DynamoDB = DynamoDB(client),
    private val mapper: DynamoDBMapper = DynamoDBMapper(client),

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    },

    private val dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

    private val namespaceRepository: NamespaceRepository = NamespaceRepository(dynamoDB, mapper, dynamoDBMapperConfig),
    private val repository: Repository<Namespace> = RepositoryImpl(dynamoDB, mapper, namespaceRepository, dynamoDBMapperConfig),

    private val nodeService : NodeService = NodeService()
) {

    fun createNamespace(namespaceRequest: WDRequest, workspaceID: String): Entity {
        val namespace: Namespace = (namespaceRequest as NamespaceRequest).toNamespace(workspaceID)
        require(!checkIfNamespaceNameExists(workspaceID, namespace.name)) { "Cannot use an existing Namespace Name" }
        return repository.create(namespace)
    }

    fun getNamespace(workspaceID: String, namespaceID: String): Namespace? {
        return namespaceRepository.get(WorkspaceIdentifier(workspaceID), NamespaceIdentifier(namespaceID), Namespace::class.java)
    }

    fun updateNamespace(namespaceRequest: WDRequest, workspaceID: String) {
        val namespace = (namespaceRequest as NamespaceRequest).toNamespace(workspaceID)
        namespaceRepository.updateNamespace(workspaceID, namespace.id, namespace)
    }

    fun updateNamespace(namespace: Namespace) {
        repository.update(namespace)
    }

    fun deleteNamespace(namespaceID: String, workspaceID: String): Identifier {
        return repository.delete(WorkspaceIdentifier(workspaceID), NamespaceIdentifier(namespaceID))
    }


    fun makeNamespacePublic(namespaceID: String, workspaceID: String) {
        require( !namespaceRepository.isNamespacePublic(namespaceID, workspaceID) ) {"Namespace already public"}
        val nodeIDList = nodeService.getAllNodesWithNamespaceIDAndAccess(namespaceID, workspaceID, 0) /* get all private nodes */
        nodeService.makeNodesPublicOrPrivateInParallel(nodeIDList, workspaceID, 1)
        namespaceRepository.setPublicAccessValue(namespaceID, workspaceID, 1)

    }

    fun makeNamespacePrivate(namespaceID: String, workspaceID: String) {
        require( namespaceRepository.isNamespacePublic(namespaceID, workspaceID) ) {"Namespace already private"}
        val nodeIDList = nodeService.getAllNodesWithNamespaceIDAndAccess(namespaceID, workspaceID, 1) /* get all public nodes */
        nodeService.makeNodesPublicOrPrivateInParallel(nodeIDList, workspaceID, 0)
        namespaceRepository.setPublicAccessValue(namespaceID, workspaceID, 0)

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

    private fun constructNamespaceInfo(namespaceList: List<Namespace>){

        val namespaceHierarchyJson: MutableMap<String, Any> = mutableMapOf()
        for (namespace in namespaceList) {
            val mapOfNamespaceData = mutableMapOf<String, Any?>()
            mapOfNamespaceData[Constants.NAME] = namespace.name
            mapOfNamespaceData[Constants.NODE_HIERARCHY] = namespace.nodeHierarchyInformation
            mapOfNamespaceData[Constants.NAMESPACE_METADATA] = namespace.namespaceMetadata
            namespaceHierarchyJson.putIfAbsent(namespace.id, mapOfNamespaceData)
        }

    }


//    fun archiveNamespace(workspaceID: String, namespaceID: String) {
//        val namespace = getNamespace(namespaceID, workspaceID).let { namespace ->
//            require(namespace != null && namespace.publicAccess) { Messages.ERROR_NAMESPACE_DOES_NOT_EXIST_OR_ARCHIVED }
//            namespace
//        }
//        archiveOrUnarchiveNamespace(namespace, ItemStatus.ACTIVE, ItemStatus.ARCHIVED)
//    }
//
//
//    fun unarchiveNamespace(workspaceID: String, namespaceID: String) {
//        val namespace = getNamespace(namespaceID, workspaceID).let { namespace ->
//            require(namespace != null && !namespace.publicAccess) { Messages.ERROR_NAMESPACE_DOES_NOT_EXIST_OR_ACTIVE }
//            namespace
//        }
//        archiveOrUnarchiveNamespace(namespace, ItemStatus.ARCHIVED, ItemStatus.ACTIVE)
//    }
//
//    fun archiveOrUnarchiveNamespace(namespace: Namespace, nodeStatus: ItemStatus, targetStatus: ItemStatus) = runBlocking{
//
//        val nodeIDList = getAllNodesWithStatus(namespace, nodeStatus) /* get all nodes with nodeStatus ( opposite of targetStatus ) */
//        launch { nodeService.unarchiveOrArchiveNodesInParallel(nodeIDList, namespace.id, targetStatus) }
//        launch { setNamespaceStatusAndHierarchy(namespace, targetStatus) }
//
//    }
//
//    private fun getAllNodesWithStatus(namespace: Namespace, nodeStatus: ItemStatus) : List<String>{
//        return when(nodeStatus){
//            ItemStatus.ACTIVE -> {
//                NodeHelper.getNodeIDsFromHierarchy(namespace.nodeHierarchyInformation)
//            }
//            ItemStatus.ARCHIVED -> {
//                NodeHelper.getNodeIDsFromHierarchy(namespace.archivedNodeHierarchyInformation)
//            }
//        }
//    }
//
//    private fun setNamespaceStatusAndHierarchy(namespace: Namespace, targetStatus: ItemStatus){
//        when(targetStatus){
//            ItemStatus.ARCHIVED -> {
//                val newArchivedHierarchy = namespace.archivedNodeHierarchyInformation.toMutableList() + namespace.nodeHierarchyInformation
//                namespace.archivedNodeHierarchyInformation = newArchivedHierarchy
//                namespace.nodeHierarchyInformation = listOf()
//            }
//            ItemStatus.ACTIVE -> {
//                val newActiveHierarchy = namespace.archivedNodeHierarchyInformation
//            }
//        }
//    }
//
//    fun isNamespaceActive(workspaceID: String, namespaceID: String) : Boolean {
//        return namespaceRepository.isNamespaceActive(workspaceID, namespaceID)
//    }
//

    companion object {
        private val LOG = LogManager.getLogger(NamespaceService::class.java)
    }
}

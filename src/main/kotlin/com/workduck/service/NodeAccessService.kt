package com.workduck.service

import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AccessType
import com.workduck.models.EntityOperationType
import com.workduck.repositories.NodeRepository
import com.workduck.utils.AccessItemHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class NodeAccessService(
        private val nodeRepository: NodeRepository,
        private val namespaceAccessService: NamespaceAccessService
) {

    // check if the user is workspace owner or has been mentioned with a given access level
    fun checkIfUserHasAccess(workspaceID: String, nodeID: String, userID: String, operationType: EntityOperationType) : Boolean{
        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)
        return checkIfNodeExistsForWorkspace(nodeID, workspaceID) || checkIfNodeSharedWithUser(nodeID, userID, accessTypeList)

    }

    fun checkIfNodeSharedWithUser(nodeID: String, userID: String, accessTypeList: List<AccessType>) : Boolean{
        return nodeRepository.checkIfUserHasAccess(nodeID, userID, accessTypeList)
    }

    fun checkIfUserHasAccessAndGetWorkspaceDetails(nodeID: String, userWorkspaceID: String, namespaceID: String, userID: String, operationType: EntityOperationType): Map<String, String> = runBlocking {

        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)

        val jobToCheckNamespaceAccess = async { namespaceAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(namespaceID, userWorkspaceID, nodeID, operationType) }
        val jobToGetNodeAccessItem = async { nodeRepository.getNodeAccessItem(nodeID, userID, accessTypeList) }

        val workspaceDetailsMap = mutableMapOf<String, String>()

        try {
            /* either the namespace exists for the user's workspace => user is the actual owner
               -OR- the namespace has been shared with the user.
             */
            val workspaceID = jobToCheckNamespaceAccess.await()[Constants.WORKSPACE_ID]!!
            jobToGetNodeAccessItem.cancel()
            workspaceDetailsMap[Constants.WORKSPACE_ID] = workspaceID

        } catch( e : IllegalArgumentException) {
            /*
            The node could have also been shared with the user, so check existence of node access record once
             */
            val nodeAccessItem = jobToGetNodeAccessItem.await() ?: throw IllegalArgumentException(Messages.ERROR_NAMESPACE_PERMISSION)
            workspaceDetailsMap[Constants.WORKSPACE_ID] = nodeAccessItem.workspace.id
            //workspaceDetailsMap[Constants.WORKSPACE_OWNER] = nodeAccessItem.ownerID

        }

//        when (jobToCheckIfNodeExistsForWorkspace.await()) {
//            true -> {
//                jobToGetNodeAccessItem.cancel()
//                workspaceDetailsMap[Constants.WORKSPACE_ID] = userWorkspaceID
//                workspaceDetailsMap[Constants.WORKSPACE_OWNER] = userID
//
//            }
//            false -> { /* if node does not exist in user's workspace, it means that the node has been shared with the user */
//                val nodeAccessItem = jobToGetNodeAccessItem.await()
//                        ?: throw IllegalArgumentException(Messages.ERROR_NAMESPACE_PERMISSION)
//
//                workspaceDetailsMap[Constants.WORKSPACE_ID] = nodeAccessItem.workspace.id
//                workspaceDetailsMap[Constants.WORKSPACE_OWNER] = nodeAccessItem.ownerID
//            }
//        }
        return@runBlocking workspaceDetailsMap
    }


    fun checkIfNodeExistsForWorkspace(nodeID: String, workspaceID: String): Boolean {
        return nodeRepository.checkIfNodeExistsForWorkspace(nodeID, workspaceID)
    }

    fun getNamespaceIDAndCheckIfUserHasAccess(userWorkspaceID: String, nodeID: String, userID: String, operationType: EntityOperationType) : Boolean {
        val nodeWorkspaceNamespacePair = nodeRepository.getNodeWorkspaceAndNamespace(nodeID)
        return checkForNodeAndNamespaceAccess(nodeWorkspaceNamespacePair, userWorkspaceID, nodeID, userID, operationType)
    }

    fun checkUserAccessAndReturnWorkspace(userWorkspaceID: String, nodeID: String, userID: String, operationType: EntityOperationType) : String {
        val nodeWorkspaceNamespacePair = nodeRepository.getNodeWorkspaceAndNamespace(nodeID)
        require(checkForNodeAndNamespaceAccess(nodeWorkspaceNamespacePair, userWorkspaceID, nodeID, userID, operationType)) {Messages.ERROR_NODE_PERMISSION}
        return nodeWorkspaceNamespacePair!!.first /* null check already present in the above function*/

    }

    private fun checkForNodeAndNamespaceAccess(nodeWorkspaceNamespacePair:  Pair<String, String>?, userWorkspaceID: String, nodeID: String, userID: String, operationType: EntityOperationType): Boolean {

        require(nodeWorkspaceNamespacePair != null) { Messages.INVALID_NODE_ID }

        val isWorkspaceOwner = nodeWorkspaceNamespacePair.first == userWorkspaceID
        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)

        return isWorkspaceOwner || checkIfNodeSharedWithUser(nodeID, userID, accessTypeList) ||
                namespaceAccessService.checkIfNamespaceSharedWithUser(nodeWorkspaceNamespacePair.second, userID, accessTypeList)

    }



    /* return workspaceID of the affected namespace */
    fun checkAccessForRefactorAndGetWorkspaceID(userWorkspaceID: String, sourceNamespaceID: String, targetNamespaceID: String, nodeID: String, userID: String) : String {
        val nodeWorkspaceNamespacePair = nodeRepository.getNodeWorkspaceAndNamespace(nodeID)
        require(nodeWorkspaceNamespacePair != null) { Messages.INVALID_NODE_ID}
        require(nodeWorkspaceNamespacePair.second == sourceNamespaceID) { Messages.INVALID_NAMESPACE_ID }

        // check if node's workspace is same as user's workspace => owner
        val isWorkspaceOwner = nodeWorkspaceNamespacePair.first == userWorkspaceID

        return when(sourceNamespaceID != targetNamespaceID){
            true -> { /* cross namespace refactor allowed only by workspace owner */
                when(isWorkspaceOwner){
                    true -> userWorkspaceID
                    false -> throw IllegalArgumentException(Messages.ERROR_NAMESPACE_PERMISSION)
                }
            }
            false -> {
                /* for same namespace refactor, either the user can be workspace owner or should have EDIT access */
                val accessTypeList = AccessItemHelper.getAccessTypesForOperation(EntityOperationType.EDIT)
                when(isWorkspaceOwner || namespaceAccessService.checkIfNamespaceSharedWithUser(nodeWorkspaceNamespacePair.second, userID, accessTypeList)){
                    true -> nodeWorkspaceNamespacePair.first
                    false -> throw IllegalArgumentException(Messages.ERROR_NAMESPACE_PERMISSION)
                }

            }
        }

    }

}
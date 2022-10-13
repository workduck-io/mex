package com.workduck.service

import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AccessType
import com.workduck.models.EntityOperationType
import com.workduck.repositories.NodeRepository
import com.workduck.utils.AccessItemHelper

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

    fun checkIfUserHasAccessAndGetWorkspaceDetails(nodeID: String, workspaceID: String, userID: String, operationType: EntityOperationType): Map<String, String> {
        var isNodeInCurrentWorkspace = false
        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)

        if (checkIfNodeExistsForWorkspace(nodeID, workspaceID)) isNodeInCurrentWorkspace = true
        else if (!checkIfNodeSharedWithUser(nodeID, userID, accessTypeList)) {
            throw NoSuchElementException(Messages.ERROR_NODE_PERMISSION)
        }

        val workspaceDetailsMap = mutableMapOf<String, String>()

        return when (isNodeInCurrentWorkspace) {
            true -> {
                workspaceDetailsMap[Constants.WORKSPACE_ID] = workspaceID
                workspaceDetailsMap[Constants.WORKSPACE_OWNER] = userID
                workspaceDetailsMap
            }
            false -> {
                nodeRepository.getNodeWorkspaceIDAndOwner(nodeID)
            }
        }
    }


    fun checkIfNodeExistsForWorkspace(nodeID: String, workspaceID: String): Boolean {
        return nodeRepository.checkIfNodeExistsForWorkspace(nodeID, workspaceID)
    }

    fun getNamespaceIDAndCheckIfUserHasAccess(userWorkspaceID: String, nodeID: String, userID: String, operationType: EntityOperationType) : Boolean {
        val nodeWorkspaceNamespacePair = nodeRepository.getNodeWorkspaceAndNamespace(nodeID)
        require(nodeWorkspaceNamespacePair != null) { Messages.INVALID_NODE_ID}

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


    fun checkIfUserHasAccessToNode(workspaceID: String, namespaceID: String, nodeID: String, userID: String, operationType: EntityOperationType) : Boolean {
        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)
        return checkIfUserHasAccess(workspaceID, nodeID, userID, operationType) || namespaceAccessService.checkIfNamespaceSharedWithUser(namespaceID, userID, accessTypeList)
    }

}
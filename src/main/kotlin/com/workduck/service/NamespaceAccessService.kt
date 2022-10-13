package com.workduck.service

import com.serverless.utils.Constants
import com.workduck.models.AccessType
import com.workduck.models.EntityOperationType
import com.workduck.repositories.NamespaceRepository
import com.workduck.utils.AccessItemHelper

class NamespaceAccessService(
        val namespaceRepository: NamespaceRepository
) {

    fun checkIfUserHasAccess(workspaceID: String, namespaceID: String, userID: String, operationType: EntityOperationType) : Boolean{
        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)
        return checkIfNamespaceExistsForWorkspace(namespaceID, workspaceID) || checkIfNamespaceSharedWithUser(namespaceID, userID, accessTypeList)
    }


    fun checkIfNamespaceSharedWithUser(namespaceID: String, userID: String, accessTypeList: List<AccessType>) : Boolean{
        return namespaceRepository.checkIfUserHasAccess(namespaceID, userID, accessTypeList)
    }


    private fun checkIfNamespaceExistsForWorkspace(nodeID: String, workspaceID: String): Boolean {
        return namespaceRepository.checkIfNamespaceExistsForWorkspace(nodeID, workspaceID)
    }

    fun checkIfUserHasAccessAndGetWorkspaceDetails(namespaceID: String, workspaceID: String, userID: String, operationType: EntityOperationType): Map<String, String> {
        var isNodeInCurrentWorkspace = false

        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)
        if (checkIfNamespaceExistsForWorkspace(namespaceID, workspaceID)) isNodeInCurrentWorkspace = true
        else if (!namespaceRepository.checkIfUserHasAccess(namespaceID, userID, accessTypeList)) {
            throw NoSuchElementException("Node you're trying to share does not exist")
        }

        val workspaceDetailsMap = mutableMapOf<String, String>()

        when (isNodeInCurrentWorkspace) {
            true -> {
                workspaceDetailsMap[Constants.WORKSPACE_ID] = workspaceID
            }
            false -> {
                workspaceDetailsMap[Constants.WORKSPACE_ID] = namespaceRepository.getWorkspaceIDOfNamespace(namespaceID)
            }
        }

        return workspaceDetailsMap
    }




}
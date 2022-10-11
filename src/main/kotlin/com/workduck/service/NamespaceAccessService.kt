package com.workduck.service

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

    fun checkIfGranterCanManageAndGetWorkspaceDetails(namespaceID: String, workspaceID: String, granterID: String): Map<String, String> {
        var isNodeInCurrentWorkspace = false

        if (checkIfNamespaceExistsForWorkspace(namespaceID, workspaceID)) isNodeInCurrentWorkspace = true
        else if (!namespaceRepository.checkIfUserHasAccess(namespaceID, granterID, listOf(AccessType.MANAGE))) {
            throw NoSuchElementException("Node you're trying to share does not exist")
        }

        val workspaceDetailsMap = mutableMapOf<String, String>()

        when (isNodeInCurrentWorkspace) {
            true -> {
                workspaceDetailsMap["workspaceID"] = workspaceID
            }
            false -> {
                workspaceDetailsMap["workspaceID"] = namespaceRepository.getWorkspaceIDOfNamespace(namespaceID)
            }
        }

        return workspaceDetailsMap
    }




}
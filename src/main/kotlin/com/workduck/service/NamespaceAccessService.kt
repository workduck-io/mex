package com.workduck.service

import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.AccessType
import com.workduck.models.EntityOperationType
import com.workduck.repositories.NamespaceRepository
import com.workduck.utils.AccessItemHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class NamespaceAccessService(
        val namespaceRepository: NamespaceRepository
) {

    fun checkIfUserHasAccess(userWorkspaceID: String, namespaceID: String, userID: String, operationType: EntityOperationType) : Boolean{
        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)
        return checkIfNamespaceExistsForWorkspace(namespaceID, userWorkspaceID) || checkIfNamespaceSharedWithUser(namespaceID, userID, accessTypeList)
    }


    fun checkIfNamespaceSharedWithUser(namespaceID: String, userID: String, accessTypeList: List<AccessType>) : Boolean{
        return namespaceRepository.checkIfUserHasAccess(namespaceID, userID, accessTypeList)
    }

    fun checkIfNamespaceExistsForWorkspace(namespaceID: String, workspaceID: String): Boolean {
        return namespaceRepository.checkIfNamespaceExistsForWorkspace(namespaceID, workspaceID)
    }

    fun checkIfUserHasAccessAndGetWorkspaceDetails(namespaceID: String, userWorkspaceID: String, userID: String, operationType: EntityOperationType): Map<String, String> = runBlocking {

        val accessTypeList = AccessItemHelper.getAccessTypesForOperation(operationType)
        val jobToCheckIfNamespaceExistsForWorkspace = async { checkIfNamespaceExistsForWorkspace(namespaceID, userWorkspaceID) }
        val jobToGetNamespaceAccessItem = async { namespaceRepository.getNamespaceAccessItem(namespaceID, userID, accessTypeList) }

        val workspaceIDOfNamespace = when (jobToCheckIfNamespaceExistsForWorkspace.await()) {
            true -> {
                jobToGetNamespaceAccessItem.cancel()
                userWorkspaceID
            }
            false -> { /* if namespace does not exist in user's workspace, it means that the namespace has been shared with the user */
                val namespaceAccessItem = jobToGetNamespaceAccessItem.await() /* if no access record for namespace, then illegal access */
                        ?: throw IllegalArgumentException(Messages.ERROR_NAMESPACE_PERMISSION)
                namespaceAccessItem.workspace.id
            }
        }

        return@runBlocking mutableMapOf(Constants.WORKSPACE_ID to workspaceIDOfNamespace)
    }


    fun getUserNamespaceAccessType(namespaceID: String, userID: String): AccessType {
        return namespaceRepository.getUserNamespaceAccessType(namespaceID, userID)
    }




}
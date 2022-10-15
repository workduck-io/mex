package com.workduck.utils

import com.serverless.utils.Constants
import com.workduck.models.AccessType
import com.workduck.models.EntityOperationType
import com.workduck.models.IdentifierType
import com.workduck.models.NamespaceAccess
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.NodeAccess
import com.workduck.models.NodeIdentifier
import com.workduck.models.WorkspaceIdentifier

object AccessItemHelper {


    private val mapOfOperationToAccessTypes = mapOf(EntityOperationType.EDIT to listOf(AccessType.MANAGE, AccessType.WRITE),
            EntityOperationType.READ to listOf(AccessType.MANAGE, AccessType.WRITE, AccessType.READ),
            EntityOperationType.MANAGE to listOf(AccessType.MANAGE))


    fun getUserIDsWithoutGranterID(userIDs: List<String>, ownerID: String): List<String> {
        return userIDs.filter { id -> id != ownerID }
    }

    fun getNodeAccessItems(nodeID: String, workspaceID: String, granterID: String, userIDList: List<String>, accessType: AccessType): List<NodeAccess> {
        return userIDList.map {
            NodeAccess(
                    node = NodeIdentifier(nodeID),
                    workspace = WorkspaceIdentifier(workspaceID),
                    granterID = granterID,
                    //ownerID = ownerID,
                    userID = it,
                    accessType = accessType
            )
        }
    }

    fun getNamespaceAccessItems(namespaceID: String, workspaceID: String, granterID: String, userIDList: List<String>, accessType: AccessType): List<NamespaceAccess> {
        return userIDList.map {
            NamespaceAccess(
                    namespace = NamespaceIdentifier(namespaceID),
                    workspace = WorkspaceIdentifier(workspaceID),
                    granterID = granterID,
                    userID = it,
                    accessType = accessType
            )
        }
    }

    fun getNodeAccessItemsFromAccessMap(nodeID: String, workspaceID: String, granterID: String, userIDToAccessMap: Map<String, AccessType>): List<NodeAccess> {
        return userIDToAccessMap.map {
            NodeAccess(
                    node = NodeIdentifier(nodeID),
                    workspace = WorkspaceIdentifier(workspaceID),
                    granterID = granterID,
                    //ownerID = ownerID,
                    userID = it.key,
                    accessType = it.value
            )
        }
    }


    fun getNodeAccessItemPK(nodeID: String) : String {
        return "${IdentifierType.NODE_ACCESS.name}${Constants.DELIMITER}$nodeID"
    }

    fun getNamespaceAccessItemPK(namespaceID: String) : String {
        return "${IdentifierType.NAMESPACE_ACCESS.name}${Constants.DELIMITER}$namespaceID"
    }



    fun getAccessTypesForOperation(operation : EntityOperationType) : List<AccessType>{
        return mapOfOperationToAccessTypes[operation]!!

    }
}
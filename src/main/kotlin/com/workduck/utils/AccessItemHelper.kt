package com.workduck.utils

import com.serverless.utils.Constants
import com.workduck.models.AccessType
import com.workduck.models.IdentifierType
import com.workduck.models.NodeAccess
import com.workduck.models.NodeIdentifier
import com.workduck.models.WorkspaceIdentifier

object AccessItemHelper {

    fun getNodeAccessItems(nodeID: String, workspaceID: String, ownerID: String, granterID: String, userIDList: List<String>, accessType: AccessType): List<NodeAccess> {
        return userIDList.map {
            NodeAccess(
                    node = NodeIdentifier(nodeID),
                    workspace = WorkspaceIdentifier(workspaceID),
                    granterID = granterID,
                    ownerID = ownerID,
                    userID = it,
                    accessType = accessType
            )
        }
    }

    fun getNodeAccessItemsFromAccessMap(nodeID: String, workspaceID: String, ownerID: String, granterID: String, userIDToAccessMap: Map<String, AccessType>): List<NodeAccess> {
        return userIDToAccessMap.map {
            NodeAccess(
                    node = NodeIdentifier(nodeID),
                    workspace = WorkspaceIdentifier(workspaceID),
                    granterID = granterID,
                    ownerID = ownerID,
                    userID = it.key,
                    accessType = it.value
            )
        }
    }


    fun getAccessItemPK(nodeID: String) : String {
        return "${IdentifierType.NODE_ACCESS.name}${Constants.DELIMITER}$nodeID"
    }
}
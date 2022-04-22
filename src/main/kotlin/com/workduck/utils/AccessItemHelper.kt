package com.workduck.utils

import com.serverless.utils.Constants
import com.workduck.models.AccessType
import com.workduck.models.IdentifierType
import com.workduck.models.NodeAccess
import com.workduck.models.NodeIdentifier

object AccessItemHelper {

    fun getNodeAccessItems(nodeID: String, ownerID: String, userIDList: List<String>, accessType: AccessType): List<NodeAccess> {
        return userIDList.map {
            NodeAccess(
                    node = NodeIdentifier(nodeID),
                    ownerID = ownerID,
                    userID = it,
                    accessType = accessType
            )
        }
    }

    fun createAccessItem(nodeID: String, mentioningUserID: String, mentionedUserID: String, accessType: AccessType) : NodeAccess{
        return NodeAccess(
                node = NodeIdentifier(nodeID),
                ownerID = mentioningUserID,
                userID = mentionedUserID,
                accessType = accessType
        )
    }

    fun getAccessItemPK(nodeID: String) : String {
        return "$nodeID${Constants.DELIMITER}${IdentifierType.NODE_ACCESS.name}"
    }
}
package com.serverless.models.requests

import com.workduck.models.AccessType


class SharedNodeRequest(

        val nodeID : String,

        val userIDs : List<String> = listOf(),

        val accessType : String = "MANAGE"


) : WDRequest {

    init {
        require(userIDs.isNotEmpty()) { "Need to provide userIDs" }

        require(nodeID.isNotEmpty()) { "NodeID can't be empty" }

        require(validateAccessTypeString(accessType)) { "Invalid AccessType" }
    }

    private fun validateAccessTypeString(accessType: String) : Boolean{
        return AccessType.values().map {
            it.name
        }.contains(accessType.uppercase())
    }

}

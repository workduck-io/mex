package com.workduck.models

interface AccessRecord {

    val workspace: WorkspaceIdentifier

    val pk : String

    val userID : String

    val granterID : String

    val accessType: AccessType

    val itemType: ItemType

}
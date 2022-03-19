package com.workduck.models

interface Page : Entity {
    var data: List<AdvancedElement>?

    val dataOrder: MutableList<String>?

    val createdBy: String?

    val lastEditedBy: String?

    val workspaceIdentifier: WorkspaceIdentifier

    val namespaceIdentifier: NamespaceIdentifier?

    val publicAccess: Boolean

    val version: Long?
}
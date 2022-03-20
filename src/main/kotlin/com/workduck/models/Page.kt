package com.workduck.models

interface Page : Entity {
    var ak: String?

    var data: List<AdvancedElement>?

    var dataOrder: MutableList<String>?

    var createdBy: String?

    val lastEditedBy: String?

    val workspaceIdentifier: WorkspaceIdentifier

    val namespaceIdentifier: NamespaceIdentifier?

    val publicAccess: Boolean

    var createdAt: Long

    val updatedAt: Long

    var version: Long?

    companion object {
        fun populatePageWithCreatedFieldsAndAK(page: Page, storedPage: Page) {
            page.createdAt = storedPage.createdAt
            page.createdBy = storedPage.createdBy
            page.ak = "${page.workspaceIdentifier.id}#${page.namespaceIdentifier?.id}"
        }
    }
}
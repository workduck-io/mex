package com.workduck.models

interface Page : Entity {

    var data: List<AdvancedElement>?

    var dataOrder: MutableList<String>?

    val title: String

    var createdBy: String?

    val lastEditedBy: String?

    val workspaceIdentifier: WorkspaceIdentifier

    val publicAccess: Boolean

    var createdAt: Long?

    val updatedAt: Long

    var version: Long?

    companion object {
        fun populatePageWithCreatedFields(page: Page, storedPage: Page) {
            page.createdAt = storedPage.createdAt
            page.createdBy = storedPage.createdBy
        }
    }
}
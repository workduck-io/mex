package com.workduck.models

interface Page<T:Element> : Entity {

    var data: List<T>?

    var dataOrder: MutableList<String>?

    val title: String

    var createdBy: String?

    val lastEditedBy: String?

    val workspaceIdentifier: WorkspaceIdentifier

    var publicAccess: Boolean

    var createdAt: Long?

    val updatedAt: Long

    var version: Int?

    var metadata : PageMetadata?

    companion object {
        fun populatePageWithCreatedAndPublicFields(page: Page<Element>, storedPage: Page<Element>) {
            page.createdAt = storedPage.createdAt
            page.createdBy = storedPage.createdBy
            page.publicAccess = storedPage.publicAccess
        }
    }
}
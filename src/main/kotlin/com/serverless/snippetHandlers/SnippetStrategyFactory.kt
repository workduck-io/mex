package com.serverless.snippetHandlers

class SnippetStrategyFactory {
    companion object {

        const val getSnippet = "GET /snippet/{id}"

        const val createSnippet = "POST /snippet"

        const val createNewVersion = "POST /snippet/newVersion"

        const val updateSameSnippetVersion = "PUT /snippet"

        const val archiveSnippet = "PATCH /snippet/archive"

        const val unarchiveSnippet = "PATCH /snippet/unarchive"

        const val deleteArchivedSnippet = "DELETE /snippet"

        const val getAllArchivedSnippets = "GET /snippet/archive/{id}"

        const val makeSnippetPublic = "PATCH /snippet/makePublic/{id}"

        const val makeSnippetPrivate = "PATCH /snippet/makePrivate/{id}"

        const val getPublicSnippet = "GET /snippet/public/{id}"

        const val clonePublicSnippet = "POST /snippet/clone"

        const val getAllSnippetVersions = "GET /snippet/{id}/all"


        private val snippetRegistry: Map<String, SnippetStrategy> = mapOf(
                getSnippet to GetSnippetStrategy(),
                createSnippet to CreateSnippetStrategy(),
                createNewVersion to CreateNewVersionStrategy(),
                updateSameSnippetVersion to UpdateSameSnippetVersionStrategy(),
                archiveSnippet to ArchiveSnippetStrategy(),
                archiveSnippet to UnarchiveSnippetStrategy(),
                deleteArchivedSnippet to DeleteArchivedSnippetStrategy(),
                getAllArchivedSnippets to GetAllArchivedSnippetsStrategy(),
                makeSnippetPublic to MakeSnippetPublicStrategy(),
                makeSnippetPrivate to MakeSnippetPrivateStrategy(),
                getPublicSnippet to GetPublicSnippetStrategy(),
                clonePublicSnippet to ClonePublicSnippetStrategy(),
                getAllSnippetVersions to GetAllSnippetVersionsStrategy()
        )

        fun getSnippetStrategy(routeKey: String): SnippetStrategy? {
            return snippetRegistry[routeKey]
        }
    }
}

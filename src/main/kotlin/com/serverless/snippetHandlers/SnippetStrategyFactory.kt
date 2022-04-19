package com.serverless.snippetHandlers

class SnippetStrategyFactory {
    companion object {

        const val getSnippet = "GET /snippet/{id}"

        const val createSnippet = "POST /snippet"

        const val createNewVersion = "POST /snippet/newVersion"

        const val updateSameSnippetVersion = "PUT /snippet"

        const val archiveSnippet = "PUT /snippet/archive"

        const val unarchiveSnippet = "PUT /snippet/unarchive"

        const val deleteSnippet = "DELETE /snippet/{id}/{version}"

        const val deleteAllVersionsOfSnippet = "DELETE /snippet/{id}/all"

        const val getAllArchivedSnippets = "GET /snippet/archive/{id}"

        const val makeSnippetPublic = "PATCH /snippet/makePublic/{id}/{version}"

        const val makeSnippetPrivate = "PATCH /snippet/makePrivate/{id}/{version}"

        const val getPublicSnippet = "GET /snippet/public/{id}/{version}"

        const val clonePublicSnippet = "POST /snippet/clone/{id}/{version}"

        const val getAllSnippetVersions = "GET /snippet/{id}/all"


        private val snippetRegistry: Map<String, SnippetStrategy> = mapOf(
                getSnippet to GetSnippetStrategy(),
                createSnippet to CreateSnippetStrategy(),
                deleteSnippet to DeleteSnippetStrategy(),
                deleteAllVersionsOfSnippet to DeleteAllVersionsOfSnippetStrategy(),
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

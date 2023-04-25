package com.serverless.snippetHandlers

class SnippetStrategyFactory {
    companion object {

        const val getSnippet = "GET /snippet/{id}"

        const val createSnippet = "POST /snippet"

        const val adminCreateSnippet = "POST /snippet/admin/{id}"

        const val createNewVersion = "POST /snippet/newVersion"

        const val updateSameSnippetVersion = "PUT /snippet"

        const val deleteSnippet = "DELETE /snippet/{id}"

        const val deleteAllVersionsOfSnippet = "DELETE /snippet/{id}/all"

        const val makeSnippetPublic = "PATCH /snippet/makePublic/{id}/{version}"

        const val makeSnippetPrivate = "PATCH /snippet/makePrivate/{id}/{version}"

        const val getPublicSnippet = "GET /snippet/public/{id}/{version}"

        const val clonePublicSnippet = "POST /snippet/clone/{id}/{version}"

        const val getAllSnippetVersions = "GET /snippet/{id}/all"

        const val getAllSnippetsOfWorkspace = "GET /snippet/all"

        const val updateMetadata = "PATCH /snippet/metadata/{id}"

        private val snippetRegistry: Map<String, SnippetStrategy> = mapOf(
                getSnippet to GetSnippetStrategy(),
                createSnippet to CreateSnippetStrategy(),
                adminCreateSnippet to AdminCreateSnippetStrategy(),
                deleteSnippet to DeleteSnippetStrategy(),
                deleteAllVersionsOfSnippet to DeleteAllVersionsOfSnippetStrategy(),
                makeSnippetPublic to MakeSnippetPublicStrategy(),
                makeSnippetPrivate to MakeSnippetPrivateStrategy(),
                getPublicSnippet to GetPublicSnippetStrategy(),
                clonePublicSnippet to ClonePublicSnippetStrategy(),
                getAllSnippetVersions to GetAllSnippetVersionsStrategy(),
                getAllSnippetsOfWorkspace to GetAllSnippetsOfWorkspaceStrategy(),
                updateMetadata to UpdateMetadataStrategy()
        )

        fun getSnippetStrategy(routeKey: String): SnippetStrategy? {
            return snippetRegistry[routeKey.replace("/v1", "")]
        }
    }
}

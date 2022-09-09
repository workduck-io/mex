package com.serverless.utils

object Constants {
    const val DELIMITER = "#"
    const val PATH_PARAMETER_SEPARATOR = "$"
    const val ID_SEPARATOR = "_"
    val NANO_ID_RANGE = "346789ABCDEFGHJKLMNPQRTUVWXYabcdefghijkmnpqrtwxyz".toCharArray()
    const val VALID_TITLE_SPECIAL_CHAR = ",:-@ _"
    fun getCurrentTime() : Long = System.currentTimeMillis()
    const val ADDED_PATHS = "addedPaths"
    const val ARCHIVED_HIERARCHY = "archivedHierarchy"
    const val REMOVED_PATHS = "removedPaths"
    const val INTERNAL_WORKSPACE = "WORKSPACE_INTERNAL"
    const val INTERNAL_NAMESPACE_ID = "NAMESPACE_DEFAULT"
    const val NODE_ID_PREFIX = "NODE_"
    const val NAMESPACE_ID_PREFIX = "NAMESPACE_"
    const val SNIPPET_ID_PREFIX = "SNIPPET_"
    const val NANO_ID_SIZE = 21
    const val DDB_MAX_ITEM_SIZE = 350000
    const val TITLE_ALPHANUMERIC_SUFFIX_SIZE = 3
    const val NODE = "node"
    const val CHANGED_PATHS = "changedPaths"
    const val NAMESPACE_INFO = "namespaceInfo"
    const val NAME = "name"
    const val NODE_HIERARCHY = "nodeHierarchy"

}

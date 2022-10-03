package com.serverless.utils

import java.time.Instant

object Constants {
    const val DELIMITER = "#"
    const val PATH_PARAMETER_SEPARATOR = "$"
    const val ID_SEPARATOR = "_"
    val NANO_ID_RANGE = "346789ABCDEFGHJKLMNPQRTUVWXYabcdefghijkmnpqrtwxyz".toCharArray()
    const val VALID_TITLE_SPECIAL_CHAR = ",:-@ _"
    fun getCurrentTime() : Long = System.currentTimeMillis()
    fun getCurrentTimeInSeconds() : Long = Instant.now().epochSecond
    const val ADDED_PATHS = "addedPaths"
    const val ARCHIVED_HIERARCHY = "archivedHierarchy"
    const val REMOVED_PATHS = "removedPaths"
    const val INTERNAL_WORKSPACE = "WORKSPACE_INTERNAL"
    const val INTERNAL_NAMESPACE_ID = "NAMESPACE_DEFAULT"
    const val NODE_ID_PREFIX = "NODE_"
    const val NAMESPACE_ID_PREFIX = "NAMESPACE_"
    const val SNIPPET_ID_PREFIX = "SNIPPET_"
    const val BLOCK_ID_PREFIX = "TEMP_"
    const val ELEMENT_TYPE_P = "p"
    const val NANO_ID_SIZE = 21
    const val MAX_NODE_IDS_FOR_BATCH_GET = 25
    const val DDB_MAX_ITEM_SIZE = 350000
    const val TITLE_ALPHANUMERIC_SUFFIX_SIZE = 3
    const val PUBLIC_NOTE_EXP_TIME_IN_SECONDS: Long = 900
    const val DEFAULT_PUBLIC_NOTE_CACHE_ENDPOINT: String = "mex-public-note-cache.m6edlo.ng.0001.use1.cache.amazonaws.com"
    const val DDB_INSERT = "INSERT"
    const val DDB_MODIFY = "MODIFY"
    const val DDB_REMOVE = "REMOVE"
    const val ENTITY_ARCHIVED_STATUS = "ARCHIVED"
    const val NODE = "node"
    const val CHANGED_PATHS = "changedPaths"
    const val NAMESPACE_INFO = "namespaceInfo"
    const val NAME = "name"
    const val NODE_HIERARCHY = "nodeHierarchy"
    const val NAMESPACE_METADATA = "namespaceMetadata"

}

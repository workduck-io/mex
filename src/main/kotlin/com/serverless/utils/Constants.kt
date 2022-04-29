package com.serverless.utils

object Constants {
    const val DELIMITER = "#"
    const val PATH_PARAMETER_SEPARATOR = "$"
    const val ID_SEPARATOR = "_"
    val NANO_ID_RANGE = "346789ABCDEFGHJKLMNPQRTUVWXYabcdefghijkmnpqrtwxyz".toCharArray()
    fun getCurrentTime() : Long = System.currentTimeMillis()
    const val ADDED_PATHS = "addedPaths"
    const val REMOVED_PATHS = "removedPaths"

}

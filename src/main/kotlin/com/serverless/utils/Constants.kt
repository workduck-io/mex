package com.serverless.utils

object Constants {
    const val DELIMITER = "#"
    const val ID_SEPARATOR = "_"
    val NANO_ID_RANGE = "346789ABCDEFGHJKLMNPQRTUVWXYabcdefghijkmnpqrtwxyz".toCharArray()
    fun getCurrentTime() : Long = System.currentTimeMillis()
}
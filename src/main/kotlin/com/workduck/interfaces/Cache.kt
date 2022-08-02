package com.workduck.interfaces

interface Cache {
    fun refreshConnection()

    fun closeConnection()

    fun get(key: String): String?

    fun set(key: String, expInSeconds: Long, value: String)
}
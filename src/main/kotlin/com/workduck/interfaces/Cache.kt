package com.workduck.interfaces

interface Cache {
    fun refreshConnection()

    fun closeConnection()

    fun get(key: String): String?

    fun set(key: String, value: String)

    fun setEx(key: String, expInSeconds: Long, value: String)
}
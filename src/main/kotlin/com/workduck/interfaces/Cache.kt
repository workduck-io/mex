package com.workduck.interfaces

interface Cache<T> {
    fun refreshConnection()

    fun closeConnection()

    fun getItem(key: String): T?

    fun setItem(key: String, expInSeconds: Long, value: T)
}
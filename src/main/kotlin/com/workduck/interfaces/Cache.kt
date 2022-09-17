package com.workduck.interfaces

interface Cache {
    fun refreshConnection()

    fun closeConnection()
}
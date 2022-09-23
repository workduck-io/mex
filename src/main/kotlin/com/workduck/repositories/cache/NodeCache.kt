package com.workduck.repositories.cache

import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.utils.Constants
import com.workduck.models.Node
import com.workduck.utils.Helper

class NodeCache(host: String = "localhost", port: Int = 6379) : Cache(host, port) {
    fun getNode(key: String): Node? {
        return super.getItem(key)?.let { node -> Helper.objectMapper.readValue(node) }
    }

    fun setNode(key: String, value: Node) {
        super.setItem(key, Constants.PUBLIC_NOTE_EXP_TIME_IN_SECONDS, value)
    }

    fun deleteNode(key: String): Boolean? {
        return this.getNode(key)?.let {
            super.deleteItem(key)?.let {
                if (it.toInt() == 1) true else false
            }
        }
    }
}
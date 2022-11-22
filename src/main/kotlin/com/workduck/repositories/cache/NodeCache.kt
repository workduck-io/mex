package com.workduck.repositories.cache

import com.serverless.utils.Constants
import com.workduck.models.Node
import com.workduck.utils.Helper

class NodeCache(host: String = "localhost", port: Int = 6379) : Cache(host, port) {
    fun getNode(key: String): Node? {
        return super.getItem(key)?.let { node ->
            return Helper.objectMapper.readValue(node, Node::class.java) }
    }

    fun setNode(key: String, value: String) {
        super.setItem(key, Constants.PUBLIC_NOTE_EXP_TIME_IN_SECONDS, value)
    }

    fun deleteNode(key: String) {
        super.deleteItem(key)
    }
}
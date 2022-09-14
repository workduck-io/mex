package com.workduck.repositories.cache

import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Node
import com.workduck.utils.Helper

class NodeCache(host: String = "localhost", port: Int = 6379) : Cache(host, port) {
    fun getNode(key: String): Node? {
        return super.getItem(key)?.let { node -> Helper.objectMapper.readValue(node) }
    }

    fun setNode(key: String, expInSeconds: Long, value: Node) {
        super.setItem(key, expInSeconds, value)
    }
}
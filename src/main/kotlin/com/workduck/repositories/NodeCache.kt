package com.workduck.repositories

import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.interfaces.Cache
import com.workduck.models.Node
import com.workduck.utils.Helper
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class NodeCache(
    private val host: String = "localhost", private val port: Int = 6379
) : Cache<Node> {
    private var jedisPoolConfig = JedisPoolConfig()
    private lateinit var jedisClient: JedisPool
    private val maxRetries = 3
    private val connectionTimeout = 3600

    init {
        for (retryIndex in 0 .. maxRetries) {
            try {
                jedisClient = JedisPool(jedisPoolConfig, host, port, connectionTimeout)
                break
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
            }
        }
    }

    override fun refreshConnection() {
        for (retryIndex in 0 .. maxRetries) {
            try {
                jedisClient = JedisPool(jedisPoolConfig, host, port, connectionTimeout)
                break
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
            }
        }
    }

    override fun closeConnection() {
        for (retryIndex in 0 .. maxRetries) {
            try {
                jedisClient.close()
                break
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
            }
        }
    }

    override fun getItem(key: String): Node? {
        for (retryIndex in 0 .. maxRetries) {
            return try {
                jedisClient.resource.get(key)?.also {
                    value -> Helper.objectMapper.readValue(value)
                }
                break
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
                null
            }
        }
        return null
    }

    override fun setItem(key: String, expInSeconds: Long, value: Node) {
        for (retryIndex in 0 .. maxRetries) {
            try {
                jedisClient.resource.setex(key, expInSeconds, Helper.objectMapper.writeValueAsString(value))
                break
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
            }
        }
    }
}
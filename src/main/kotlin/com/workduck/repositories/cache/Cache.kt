package com.workduck.repositories.cache

import com.workduck.interfaces.Cache
import com.workduck.utils.Helper
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

open class Cache(private val host: String = "localhost", private val port: Int = 6379): Cache {
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

    protected fun getItem(key: String): String? {
        for (retryIndex in 0 .. maxRetries) {
            return try {
                return jedisClient.resource.get(key)
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
                null
            }
        }
        return null
    }

    protected fun setItem(key: String, expInSeconds: Long, value: Any) {
        for (retryIndex in 0 .. maxRetries) {
            try {
                jedisClient.resource.setex(key, expInSeconds, Helper.objectMapper.writeValueAsString(value))
                break
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
            }
        }
    }

    protected fun deleteItem(key: String): Long? {
        for (retryIndex in 0 .. maxRetries) {
            try {
                return jedisClient.resource.del(key)
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
            }
        }
        return null
    }

}
package com.workduck.repositories

import com.workduck.interfaces.Cache
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class Cache(private val host: String = "localhost", private val port: Int = 6379) : Cache {
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

    override fun get(key: String): String? {
        for (retryIndex in 0 .. maxRetries) {
            return try {
                jedisClient.resource.get(key)
                break
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
                null
            }
        }
        return null
    }

    override fun set(key: String, expInSeconds: Long, value: String) {
        for (retryIndex in 0 .. maxRetries) {
            try {
                jedisClient.resource.setex(key, expInSeconds, value)
                break
            } catch (e: Throwable) {
                if (retryIndex == maxRetries) throw e
            }
        }
    }
}
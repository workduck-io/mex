package com.workduck.repositories

import com.workduck.interfaces.Cache
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class Cache(private val host: String = "localhost", private val port: Int = 6379) : Cache {
    private var jedisPoolConfig = JedisPoolConfig()
    private var jedisClient: JedisPool
    private var maxRetries = 3

    init {
        jedisClient = JedisPool(jedisPoolConfig, host, port, 1800)
    }

    override fun refreshConnection() {
        jedisClient = JedisPool(host, port)
    }

    override fun closeConnection() {
        jedisClient.close()
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
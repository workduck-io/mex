package com.workduck.repositories

import com.serverless.ddbStreamTriggers.publicnoteUpdateTrigger.PublicNoteUpdate
import com.workduck.interfaces.Cache
import org.apache.logging.log4j.LogManager
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool


class Cache(private val host: String = "localhost", private val port: Int = 6379) : Cache {
    private var jedisClient = JedisPool(host, port)

    override fun refreshConnection() {
        jedisClient = JedisPool(host, port)
    }

    override fun closeConnection() {
        jedisClient.close()
    }

    override fun get(key: String): String? {
        return try {
            jedisClient.resource.get(key)
        } catch (ex: Exception) {
            LOG.debug(ex.message.toString())
            null
        }
    }

    override fun set(key: String, value: String) {
        try {
            jedisClient.resource.set(key, value)
        } catch (ex: Exception) {
            LOG.debug(ex.message.toString())
        }
    }

    override fun setEx(key: String, expInSeconds: Long, value: String) {
        try {
            jedisClient.resource.setex(key, expInSeconds, value)
        } catch (ex: Exception) {
            LOG.debug(ex.message.toString())
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(Cache::class.java)
    }

}
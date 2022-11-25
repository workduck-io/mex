package com.serverless.utils

object CacheHelper {
    const val CACHE_DELIMITER = '+'

    fun encodePublicCacheKey(nodeId: String): String {
        return "PUBLIC${CACHE_DELIMITER}${nodeId}"
    }
}
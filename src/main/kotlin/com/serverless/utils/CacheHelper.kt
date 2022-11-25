package com.serverless.utils

object CacheHelper {
    const val CACHE_DELIMITER = '+'

    fun encodePublicCacheKey(nodeId: String): String {
        return "MEX-BACKEND${CACHE_DELIMITER}${nodeId}"
    }
}
package com.serverless.ddbStreamTriggers.publicnoteUpdateTrigger

import org.apache.logging.log4j.LogManager

class DDBUtil {

    fun unmarshall(ddbJSON: Map<Any, Any>): Map<Any, Any> {
     return emptyMap()
    }
    companion object {
        private val LOG = LogManager.getLogger(DDBUtil::class.java)
    }
}
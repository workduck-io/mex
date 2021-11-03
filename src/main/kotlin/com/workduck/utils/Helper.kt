package com.workduck.utils

import java.util.*

object Helper {

    private fun uuidBase32(prefix: String): String? {
        return uuidBase32(UUID.randomUUID(), java.lang.StringBuilder(prefix)).toString()
    }

    private fun uuidBase32(uuid: UUID, builder: StringBuilder): StringBuilder? {
        Base32.encode(uuid.mostSignificantBits, builder)
        Base32.encode(uuid.leastSignificantBits, builder)
        return builder
    }

    fun generateId(prefix: String?): String {
        return uuidBase32(UUID.randomUUID(), StringBuilder(prefix)).toString()
    }
}

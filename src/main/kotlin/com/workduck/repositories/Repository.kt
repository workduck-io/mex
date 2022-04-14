package com.workduck.repositories

import com.workduck.models.Entity
import com.workduck.models.Identifier

interface Repository<T : Entity> {
    fun create(t: T): T

    fun update(t: T): T

    fun get(pkIdentifier: Identifier, skIdentifier: Identifier, clazz: Class<T>): T?

    fun delete(pkIdentifier: Identifier, skIdentifier: Identifier): Identifier

}

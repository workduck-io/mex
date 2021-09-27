package com.workduck.repositories

import com.workduck.models.Element
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Node

interface Repository<T: Entity> {
    fun create(t: T): T

    fun update(t:T): T

    fun get(identifier: Identifier) : Node

    fun delete(identifier: Identifier, tableName: String)

    fun append(identifier: Identifier, tableName: String, elements : MutableList<Element>)
}
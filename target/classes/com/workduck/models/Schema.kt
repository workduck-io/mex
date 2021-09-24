package com.workduck.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.utils.Helper
import kotlin.reflect.KClass

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = NodeSchema::class, name = "node-schema")
)
sealed class Schema(
    open val version : Int,
    open val authorizations: Set<Auth>
)
@JsonTypeName("node-schema")
data class NodeSchema(
    val id: String = Helper.generateId("NSM"),
    override val version: Int,
    val allowedProperties: List<KClass<Element>>,
    override val authorizations: Set<Auth>
): Schema(version, authorizations), Entity {
//    override val partitionKey: String
//        get() = TODO("Not yet implemented")
//
//    override val sortKey: List<String>
//        get() = TODO("Not yet implemented")
}

//
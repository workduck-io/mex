package com.workduck.models

/**
 * namespace status
 */
enum class NamespaceStatus{
    ACTIVE,
    INACTIVE
}

/**
 * class for namespace
 */
class Namespace(
    val authorizations : Set<Auth>,
    val name: String,
    val owner: OwnerIdentifier,
    val createdAt: Long,
    val status: NamespaceStatus = NamespaceStatus.ACTIVE
){
    val updatedAt = System.currentTimeMillis()
}
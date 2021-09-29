package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.utils.Helper

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

@DynamoDBTable(tableName="elementsTableTest")
class Namespace(
    //val authorizations : Set<Auth>,

    @JsonProperty("id")
    @DynamoDBHashKey(attributeName="PK")
    var id : String = Helper.generateId(IdentifierType.NAMESPACE.name),

    @JsonProperty("name")
    @DynamoDBAttribute(attributeName="namespaceName")
    var name: String = "",


    @JsonProperty("workspaceIdentifier")
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName="workspaceIdentifier")
    var workspaceIdentifier: WorkspaceIdentifier? = null,

    //val owner: OwnerIdentifier,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName="createdAt")
    var createdAt: Long = System.currentTimeMillis(),
    //val status: NamespaceStatus = NamespaceStatus.ACTIVE
) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName="updatedAt")
    var updatedAt = System.currentTimeMillis()
}
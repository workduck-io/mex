package com.workduck.models


import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.convertersv2.ItemTypeConverterV2
import com.workduck.convertersv2.WorkspaceIdentifierConverterV2
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

/**
 * namespace status
 */
enum class NamespaceStatus {
    ACTIVE,
    INACTIVE
}

/**
 * class for namespace
 */


class Namespace(
    // val authorizations : Set<Auth>,

    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("PK")
    var id: String = Helper.generateId(IdentifierType.NAMESPACE.name),

    /* For convenient deletion */
    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var idCopy: String = id,

    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @get:DynamoDbConvertedBy(WorkspaceIdentifierConverterV2::class)
    var workspaceIdentifier: WorkspaceIdentifier? = null,

    var name: String = "DEFAULT_NAMESPACE",

    // val owner: OwnerIdentifier,

    var createdAt: Long? = Constants.getCurrentTime(),

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    override var itemType: ItemType = ItemType.Namespace

    // val status: NamespaceStatus = NamespaceStatus.ACTIVE

) : Entity {

    companion object {
        val NAMESPACE_TABLE_SCHEMA: TableSchema<Namespace> = TableSchema.fromClass(Namespace::class.java)
    }

    var updatedAt = Constants.getCurrentTime()
}

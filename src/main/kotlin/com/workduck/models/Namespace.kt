package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.utils.Helper
import javax.naming.Name

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

@DynamoDBTable(tableName = "sampleData")
class Namespace(
    // val authorizations : Set<Auth>,

    @JsonProperty("workspaceIdentifier")
    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBHashKey(attributeName = "PK")
    var workspaceIdentifier: WorkspaceIdentifier = WorkspaceIdentifier("DefaultWorkspace"),

    /* For convenient deletion */
    @JsonProperty("id")
    @DynamoDBRangeKey(attributeName = "SK")
    var id: String = Helper.generateNanoID(IdentifierType.NAMESPACE.name),


    @JsonProperty("name")
    @DynamoDBAttribute(attributeName = "namespaceName")
    var name: String = "DEFAULT_NAMESPACE",

    // val owner: OwnerIdentifier,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = Constants.getCurrentTime(),

    @JsonProperty("createdBy")
    @DynamoDBAttribute(attributeName = "createdBy")
    var createdBy: String? = null,

    @JsonProperty("lastEditedBy")
    @DynamoDBAttribute(attributeName = "lastEditedBy")
    var lastEditedBy: String? = null,

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.Namespace,

    @JsonProperty("nodeHierarchyInformation")
    @DynamoDBAttribute(attributeName = "nodeHierarchyInformation")
    var nodeHierarchyInformation: List<String> ? = null,

    // val status: NamespaceStatus = NamespaceStatus.ACTIVE

) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt = Constants.getCurrentTime()

}

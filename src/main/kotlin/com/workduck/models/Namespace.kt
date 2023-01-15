package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.NamespaceIdentifierConverter
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.converters.NamespaceMetadataConverter
import com.workduck.converters.NamespaceMetadataDeserializer
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.utils.Helper


/**
 * class for namespace
 */

@DynamoDBTable(tableName = "local-mex")
@JsonIgnoreProperties(ignoreUnknown = true)
class Namespace(
    // val authorizations : Set<Auth>,

    @JsonAlias("PK")
    @JsonProperty("workspaceIdentifier")
    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBHashKey(attributeName = "PK")
    var workspaceIdentifier: WorkspaceIdentifier = WorkspaceIdentifier("DefaultNamespace"),


    @JsonProperty("id")
    @JsonAlias("SK")
    @DynamoDBRangeKey(attributeName = "SK")
    var id: String = Helper.generateNanoID(IdentifierType.NAMESPACE.name),


    @JsonProperty("name")
    @JsonAlias("namespaceName")
    @DynamoDBAttribute(attributeName = "namespaceName")
    var name: String = "DEFAULT_NAMESPACE",


    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = Constants.getCurrentTime(),

    @JsonProperty("createdBy")
    @DynamoDBAttribute(attributeName = "createdBy")
    var createdBy: String? = null,

    @JsonProperty("metadata")
    @DynamoDBTypeConverted(converter = NamespaceMetadataConverter::class)
    @JsonDeserialize(converter = NamespaceMetadataDeserializer::class)
    @DynamoDBAttribute(attributeName = "metadata")
    var namespaceMetadata : NamespaceMetadata ?= null,


    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.Namespace,

    @JsonProperty("nodeHierarchyInformation")
    @DynamoDBAttribute(attributeName = "nodeHierarchyInformation")
    var nodeHierarchyInformation: List<String> = listOf(),

    @JsonProperty("publicAccess")
    @DynamoDBAttribute(attributeName = "publicAccess")
    var publicAccess: Boolean = false,

    @JsonProperty("deleted")
    @DynamoDBAttribute(attributeName = "deleted")
    var deleted: Boolean = false,

    /* only to be used when namespace is deleted and the nodes need to be moved */
    @JsonProperty("successorNamespace")
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = NamespaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "successorNamespace")
    var successorNamespace: NamespaceIdentifier? = null,


    @JsonProperty("archivedNodeHierarchyInformation")
    @DynamoDBAttribute(attributeName = "archivedNodeHierarchyInformation")
    var archivedNodeHierarchyInformation: List<String> = listOf(),

) : Entity {

    companion object {
        fun populateHierarchiesAndUpdatedAt(namespace: Namespace, activeHierarchy : List<String>?, archivedHierarchy : List<String>?, updatedAt: Long = Constants.getCurrentTime()){
            activeHierarchy?.let { namespace.nodeHierarchyInformation = it }
            archivedHierarchy?.let { namespace.archivedNodeHierarchyInformation = it }
            namespace.updatedAt = updatedAt
        }

        fun getSuccessorNamespaceDTO(namespace: Namespace) : SuccessorNamespaceDTO{
            check(namespace.successorNamespace != null) { Messages.SUCCESSOR_NAMESPACE_NULL }
            return SuccessorNamespaceDTO(
                id = namespace.successorNamespace!!.id
            )
        }

        fun getNamespaceDTO(namespace: Namespace) : NamespaceDTO {
            check(namespace.createdBy != null) { Messages.CREATED_BY_NULL }
            return NamespaceDTO(
                id = namespace.id,
                workspaceID = namespace.workspaceIdentifier.id,
                createdBy = namespace.createdBy!!
            )
        }
    }

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt = Constants.getCurrentTime()


    @JsonProperty("expireAt")
    @DynamoDBAttribute(attributeName = "expireAt")
    var expireAt: Long? = null

}

data class SuccessorNamespaceDTO(
    val id: String,

)

data class NamespaceDTO(
    val id: String,
    val workspaceID : String,
    val createdBy: String

)

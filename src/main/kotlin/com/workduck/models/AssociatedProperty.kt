package com.workduck.models


import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.convertersv2.ItemTypeConverterV2
import com.workduck.convertersv2.NodeIdentifierListConverterV2
import com.workduck.convertersv2.WorkspaceIdentifierConverterV2
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

/**
 * All associated property type
 */
enum class AssociatedPropertyType {
    TAG
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Tag::class, name = "tag")
)
sealed class AssociatedProperty(
    propertyType: AssociatedPropertyType
)

@JsonTypeName("tag")
data class Tag(


    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @get:DynamoDbConvertedBy(WorkspaceIdentifierConverterV2::class)
    @get:DynamoDbAttribute("PK")
    @get:DynamoDbPartitionKey
    var workspaceIdentifier: WorkspaceIdentifier = WorkspaceIdentifier("DefaultWorkspace"),

    // cannot be kept as empty string due to DDB constraints.
    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var name: String = "New Tag",

    var createdAt: Long = Constants.getCurrentTime(),

    @get:DynamoDbConvertedBy(NodeIdentifierListConverterV2::class)
    var nodes: List<NodeIdentifier>? = null,

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    override var itemType: ItemType = ItemType.Tag

) : AssociatedProperty(AssociatedPropertyType.TAG), Entity {

    var updatedAt: Long = Constants.getCurrentTime()

    companion object {
        val TAG_TABLE_SCHEMA: TableSchema<Tag> = TableSchema.fromClass(Tag::class.java)
    }

    init {
        require(name.isNotEmpty()) { "Tag name cannot be empty" }
    }

}

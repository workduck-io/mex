package com.workduck.models


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.serverless.utils.Constants
import com.workduck.converters.HierarchyUpdateSourceConverter
import com.workduck.convertersv2.HierarchyUpdateSourceConverterV2
import com.workduck.convertersv2.ItemTypeConverterV2
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

enum class HierarchyUpdateSource {
    REFRESH, /* when refresh endpoint is used or would be used */
    NODE, /* when nodes are added */
    ARCHIVE, /* when a node is archived, we first update the hierarchy */
    RENAME
}

class Workspace(

    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("PK")
    var id: String = Helper.generateId(IdentifierType.WORKSPACE.name),

    /* For convenient deletion */
    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var idCopy: String = id,

    var name: String = "DEFAULT_WORKSPACE",

    var createdAt: Long? = Constants.getCurrentTime(),

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    override var itemType: ItemType = ItemType.Workspace,

    var nodeHierarchyInformation: List<String> ? = null,

    var archivedNodeHierarchyInformation: List<String> ? = null,

    @get:DynamoDbConvertedBy(HierarchyUpdateSourceConverterV2::class)
    @DynamoDBTypeConverted(converter = HierarchyUpdateSourceConverter::class)
    var hierarchyUpdateSource: HierarchyUpdateSource = HierarchyUpdateSource.REFRESH

) : Entity {

    companion object {

        val WORKSPACE_TABLE_SCHEMA: TableSchema<Workspace> = TableSchema.fromClass(Workspace::class.java)

        fun populateHierarchiesAndUpdatedAt(workspace: Workspace, activeHierarchy: List<String>, archivedHierarchy: List<String>?, updatedAt: Long = Constants.getCurrentTime()) {
            workspace.nodeHierarchyInformation = activeHierarchy
            workspace.archivedNodeHierarchyInformation = archivedHierarchy
            workspace.updatedAt = updatedAt
        }
    }

    var updatedAt: Long = Constants.getCurrentTime()
}

package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.converters.HierarchyUpdateSourceConverter
import com.workduck.converters.ItemTypeConverter
import com.workduck.utils.Helper

enum class HierarchyUpdateSource {
    REFRESH, /* when refresh endpoint is used or would be used */
    NODE, /* when nodes are added */
    ARCHIVE /* when a node is archived, we first update the hierarchy */
}



@DynamoDBTable(tableName = "sampleData")
class Workspace(

    @JsonProperty("id")
    @DynamoDBHashKey(attributeName = "PK")
    var id: String = Helper.generateId(IdentifierType.WORKSPACE.name),

    /* For convenient deletion */
    @JsonProperty("idCopy")
    @DynamoDBRangeKey(attributeName = "SK")
    var idCopy: String = id,

    @JsonProperty("name")
    @DynamoDBAttribute(attributeName = "workspaceName")
    var name: String = "DEFAULT_WORKSPACE",

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = System.currentTimeMillis(),

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: String = "Workspace",


    @JsonProperty("nodeHierarchyInformation")
    @DynamoDBAttribute(attributeName = "nodeHierarchyInformation")
    var nodeHierarchyInformation : List<String> ?= null,

    @JsonProperty("hierarchyUpdateSource")
    @DynamoDBAttribute(attributeName = "hierarchyUpdateSource")
    @DynamoDBTypeConverted(converter = HierarchyUpdateSourceConverter::class)
    var hierarchyUpdateSource : HierarchyUpdateSource = HierarchyUpdateSource.NODE

) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = System.currentTimeMillis()

}

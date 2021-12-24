package com.serverless.sqsEventHandlers

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.converters.NodeDataImageDeserializer
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.converters.IdentifierSerializer
import com.workduck.models.AdvancedElement
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.WorkspaceIdentifier
import com.workduck.models.NodeSchemaIdentifier

data class NodeImage(

    @JsonProperty("PK")
    var id: String? = null,

    /* For convenient deletion */
    @JsonProperty("SK")
    var idCopy: String? = id,

    @JsonProperty("lastEditedBy")
    var lastEditedBy: String? = null,

    @JsonProperty("createdBy")
    var createdBy: String? = null,

    @JsonProperty("data")
    @JsonDeserialize(converter = NodeDataImageDeserializer::class)
    var data: List<AdvancedElement>? = null,

    // TODO(write converter to store as map in DDB. And create Tag class)
    @JsonProperty("tags")
    var tags: MutableList<String>? = null,

    @JsonProperty("dataOrder")
    var dataOrder: MutableList<String>? = null,

    @JsonProperty("version")
    var version: Long? = null,

    @JsonProperty("namespaceIdentifier")
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    var namespaceIdentifier: NamespaceIdentifier? = null,

    @JsonProperty("workspaceIdentifier")
    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    var workspaceIdentifier: WorkspaceIdentifier? = null,

    /* WORKSPACE_ID#NAMESPACE_ID */
    @JsonProperty("AK")
    var ak: String? = null,

    @JsonProperty("nodeSchemaIdentifier")
    var nodeSchemaIdentifier: NodeSchemaIdentifier? = null,

    // @JsonProperty("status")
    // val status: NodeStatus = NodeStatus.LINKED,
    // val associatedProperties: Set<AssociatedProperty>,

    @JsonProperty("itemType")
    var itemType: String = "Node",

    @JsonProperty("itemStatus")
    var itemStatus: String = "ACTIVE",

    @JsonProperty("isBookmarked")
    // TODO(make it part of NodeResponse object in code cleanup)
    var isBookmarked: Boolean? = null,

    @JsonProperty("publicAccess")
    var publicAccess: Boolean = false,

    @JsonProperty("createdAt")
    var createdAt: Long? = System.currentTimeMillis(),

    @JsonProperty("updatedAt")
    var updatedAt: Long = System.currentTimeMillis(),

    @JsonProperty("lastVersionCreatedAt")
    var lastVersionCreatedAt: Long? = null,

    @JsonProperty("nodeVersionCount")
    var nodeVersionCount: Long = 0

) : Image

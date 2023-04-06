package com.workduck.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.utils.Helper

enum class IdentifierType(s: String) {
    OWNER("OWN"),
    NAMESPACE("NAMESPACE"),
    NODE("NODE"),
    NODE_ACCESS("NODE-ACCESS"),
    NAMESPACE_ACCESS("NAMESPACE_ACCESS"),
    ASSOCIATED_PROPERTY("AS-PROPERTY"),
    NODE_SCHEMA("NODE-SCHEMA"),
    RELATIONSHIP("RELATIONSHIP"),
    WORKSPACE("WORKSPACE"),
    USER("USER"),
    BOOKMARK("BOOKMARK"),
    COMMENT("COMMENT"),
    SNIPPET("SNIPPET"),
    SMART_CAPTURE("SMART_CAPTURE"),
    HIGHLIGHT("HIGHLIGHT"),

}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "mapping"
)
@JsonSubTypes(

    JsonSubTypes.Type(value = OwnerIdentifier::class, name = "owner"),
    JsonSubTypes.Type(value = NamespaceIdentifier::class, name = "namespace"),
    JsonSubTypes.Type(value = NodeIdentifier::class, name = "node"),
    JsonSubTypes.Type(value = NodeAccessIdentifier::class, name = "nodeAccess"),
    JsonSubTypes.Type(value = SnippetIdentifier::class, name = "snippet"),
    JsonSubTypes.Type(value = AssociatedPropertyIdentifier::class, name = "as-property"),
    JsonSubTypes.Type(value = NodeSchemaIdentifier::class, name = "node-schema"),
    JsonSubTypes.Type(value = RelationshipIdentifier::class, name = "relationship"),
    JsonSubTypes.Type(value = WorkspaceIdentifier::class, name = "workspace"),
    JsonSubTypes.Type(value = RelationshipIdentifier::class, name = "relationship"),
    JsonSubTypes.Type(value = BookmarkIdentifier::class, name = "bookmark"),
    JsonSubTypes.Type(value = CommentIdentifier::class, name = "comment")

)
sealed class Identifier(
    val type: IdentifierType,
    open var id: String
)

@JsonTypeName("owner")
data class OwnerIdentifier(
    override var id: String = Helper.generateNanoID(IdentifierType.OWNER.name)
) : Identifier(IdentifierType.OWNER, id)

@JsonTypeName("namespace")
data class NamespaceIdentifier(
    override var id: String = Helper.generateNanoID(IdentifierType.NAMESPACE.name)
) : Identifier(IdentifierType.NAMESPACE, id)

@JsonTypeName("node")
data class NodeIdentifier(
    override var id: String = Helper.generateNanoID(IdentifierType.NODE.name)
) : Identifier(IdentifierType.NODE, id)


@JsonTypeName("nodeAccess")
data class NodeAccessIdentifier(
        override var id: String = Helper.generateNanoID(IdentifierType.NODE_ACCESS.name)
) : Identifier(IdentifierType.NODE_ACCESS, id)

@JsonTypeName("snippet")
data class SnippetIdentifier(
        override var id: String = Helper.generateNanoID(IdentifierType.SNIPPET.name)
) : Identifier(IdentifierType.SNIPPET, id)

@JsonTypeName("as-property")
data class AssociatedPropertyIdentifier(
    val associatedPropertyType: AssociatedPropertyType,
    override var id: String = Helper.generateNanoID(IdentifierType.ASSOCIATED_PROPERTY.name)
) : Identifier(IdentifierType.ASSOCIATED_PROPERTY, id)

@JsonTypeName("node-schema")
data class NodeSchemaIdentifier(
    override var id: String = Helper.generateNanoID(IdentifierType.NODE_SCHEMA.name)
) : Identifier(IdentifierType.NODE_SCHEMA, id)

@JsonTypeName("relationship")
data class RelationshipIdentifier(
    override var id: String = Helper.generateNanoID(IdentifierType.RELATIONSHIP.name)
) : Identifier(IdentifierType.RELATIONSHIP, id)

@JsonTypeName("workspace")
data class WorkspaceIdentifier(
    override var id: String = Helper.generateNanoID(IdentifierType.WORKSPACE.name)
) : Identifier(IdentifierType.WORKSPACE, id)

@JsonTypeName("workspace")
data class BookmarkIdentifier(
    override var id: String = Helper.generateNanoID(IdentifierType.BOOKMARK.name)
) : Identifier(IdentifierType.BOOKMARK, id)

@JsonTypeName("user")
data class UserIdentifier(
    override var id: String = Helper.generateNanoID(IdentifierType.USER.name)
) : Identifier(IdentifierType.USER, id)

@JsonTypeName("comment")
data class CommentIdentifier(
        override var id: String = Helper.generateNanoID(IdentifierType.COMMENT.name)
) : Identifier(IdentifierType.COMMENT, id)
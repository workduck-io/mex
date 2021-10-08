package com.workduck.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.utils.Helper


enum class IdentifierType(s: String) {
	OWNER("OWN"),
	NAMESPACE("NAMESPACE"),
	NODE("NODE"),
	ASSOCIATED_PROPERTY("AS-PROPERTY"),
	NODE_SCHEMA("NODE-SCHEMA"),
	RELATIONSHIP("RELATIONSHIP"),
	WORKSPACE("WORKSPACE"),
	USER("USER")

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
	JsonSubTypes.Type(value = AssociatedPropertyIdentifier::class, name = "as-property"),
	JsonSubTypes.Type(value = NodeSchemaIdentifier::class, name = "node-schema"),
	JsonSubTypes.Type(value = RelationshipIdentifier::class, name = "relationship"),
	JsonSubTypes.Type(value = WorkspaceIdentifier::class, name = "workspace"),
	JsonSubTypes.Type(value = RelationshipIdentifier::class, name = "relationship")

)
sealed class Identifier(
	val type: IdentifierType,
	open var id: String
)

@JsonTypeName("owner")
data class OwnerIdentifier(
	override var id: String = Helper.generateId(IdentifierType.OWNER.name)
) : Identifier(IdentifierType.OWNER, id)

@JsonTypeName("namespace")
data class NamespaceIdentifier(
	override var id: String = Helper.generateId(IdentifierType.NAMESPACE.name)
) : Identifier(IdentifierType.NAMESPACE, id)

@JsonTypeName("node")
data class NodeIdentifier(
	override var id: String = Helper.generateId(IdentifierType.NODE.name)
) : Identifier(IdentifierType.NODE, id)

@JsonTypeName("as-property")
data class AssociatedPropertyIdentifier(
	val associatedPropertyType: AssociatedPropertyType,
	override var id: String = Helper.generateId(IdentifierType.ASSOCIATED_PROPERTY.name)
) : Identifier(IdentifierType.ASSOCIATED_PROPERTY, id)


@JsonTypeName("node-schema")
data class NodeSchemaIdentifier(
	override var id: String = Helper.generateId(IdentifierType.NODE_SCHEMA.name)
) : Identifier(IdentifierType.NODE_SCHEMA, id)

@JsonTypeName("relationship")
data class RelationshipIdentifier(
	override var id: String = Helper.generateId(IdentifierType.RELATIONSHIP.name)
) : Identifier(IdentifierType.RELATIONSHIP, id)


@JsonTypeName("workspace")
data class WorkspaceIdentifier(
	override var id: String = Helper.generateId(IdentifierType.WORKSPACE.name)
) : Identifier(IdentifierType.WORKSPACE, id)

@JsonTypeName("user")
data class UserIdentifier(
	override var id: String = Helper.generateId(IdentifierType.USER.name)
) : Identifier(IdentifierType.USER, id)
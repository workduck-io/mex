package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = GenericListRequest::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = NodeRequest::class, name = "NodeRequest"),
    JsonSubTypes.Type(value = SharedNodeRequest::class, name = "SharedNodeRequest"),
    JsonSubTypes.Type(value = UpdateSharedNodeRequest::class, name = "UpdateSharedNodeRequest"),
    JsonSubTypes.Type(value = SharedNamespaceRequest::class, name = "SharedNamespaceRequest"),
    JsonSubTypes.Type(value = UpdateAccessTypesRequest::class, name = "UpdateAccessTypesRequest"),
    JsonSubTypes.Type(value = SmartCaptureRequest::class, name = "SmartCaptureRequest"),
    JsonSubTypes.Type(value = MoveEntityRequest::class, name = "MoveEntityRequest"),
    JsonSubTypes.Type(value = SnippetRequest::class, name = "SnippetRequest"),
    JsonSubTypes.Type(value = ElementRequest::class, name = "ElementRequest"),
    JsonSubTypes.Type(value = WorkspaceRequest::class, name = "WorkspaceRequest"),
    JsonSubTypes.Type(value = NamespaceRequest::class, name = "NamespaceRequest"),
    JsonSubTypes.Type(value = BookmarkRequest::class, name = "BookmarkRequest"),
    JsonSubTypes.Type(value = UserPreferenceRequest::class, name = "UserPreferenceRequest"),
    JsonSubTypes.Type(value = BlockMovementRequest::class, name = "BlockMovementRequest"),
    JsonSubTypes.Type(value = RefactorRequest::class, name = "RefactorRequest"),
    JsonSubTypes.Type(value = NodeBulkRequest::class, name = "NodeBulkRequest"),
    JsonSubTypes.Type(value = TagRequest::class, name = "TagRequest"),
    JsonSubTypes.Type(value = RegisterUserRequest::class, name = "RegisterUserRequest"),
    JsonSubTypes.Type(value = RegisterWorkspaceRequest::class, name = "RegisterWorkspaceRequest"),
    JsonSubTypes.Type(value = MetadataRequest::class, name = "MetadataRequest"),
    JsonSubTypes.Type(value = SuccessorNamespaceRequest::class, name = "SuccessorNamespaceRequest"),
    JsonSubTypes.Type(value = SingleElementRequest::class, name = "SingleElementRequest")
)
interface WDRequest

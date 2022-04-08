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
    JsonSubTypes.Type(value = SnippetRequest::class, name = "SnippetRequest"),
    JsonSubTypes.Type(value = CloneSnippetRequest::class, name = "CloneSnippetRequest"),
    JsonSubTypes.Type(value = ElementRequest::class, name = "ElementRequest"),
    JsonSubTypes.Type(value = WorkspaceRequest::class, name = "WorkspaceRequest"),
    JsonSubTypes.Type(value = NamespaceRequest::class, name = "NamespaceRequest"),
    JsonSubTypes.Type(value = BookmarkRequest::class, name = "BookmarkRequest"),
    JsonSubTypes.Type(value = UserPreferenceRequest::class, name = "UserPreferenceRequest"),
    JsonSubTypes.Type(value = CommentRequest::class, name = "CommentRequest"),
    JsonSubTypes.Type(value = BlockMovementRequest::class, name = "BlockMovementRequest"),
    JsonSubTypes.Type(value = CommentRequest::class, name = "CommentRequest"),
    JsonSubTypes.Type(value = RefactorRequest::class, name = "RefactorRequest"),
    JsonSubTypes.Type(value = RegisterUserRequest::class, name = "RegisterUserRequest")
)
interface WDRequest

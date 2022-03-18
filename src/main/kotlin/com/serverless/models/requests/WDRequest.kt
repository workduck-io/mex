package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.serverless.models.requests.BookmarkRequest
import com.serverless.models.requests.CommentRequest
import com.serverless.models.requests.ElementRequest
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.NamespaceRequest
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.UserPreferenceRequest
import com.serverless.models.requests.WorkspaceRequest

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = GenericListRequest::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = NodeRequest::class, name = "NodeRequest"),
    JsonSubTypes.Type(value = ElementRequest::class, name = "ElementRequest"),
    JsonSubTypes.Type(value = WorkspaceRequest::class, name = "WorkspaceRequest"),
    JsonSubTypes.Type(value = NamespaceRequest::class, name = "NamespaceRequest"),
    JsonSubTypes.Type(value = BookmarkRequest::class, name = "BookmarkRequest"),
    JsonSubTypes.Type(value = UserPreferenceRequest::class, name = "UserPreferenceRequest"),
    JsonSubTypes.Type(value = CommentRequest::class, name = "CommentRequest"),
    JsonSubTypes.Type(value = BlockMovementRequest::class, name = "BlockMovementRequest"),
    JsonSubTypes.Type(value = RegisterUserRequest::class, name = "RegisterUserRequest")
)
interface WDRequest

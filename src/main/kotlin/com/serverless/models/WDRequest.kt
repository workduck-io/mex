package com.serverless.models

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
    JsonSubTypes.Type(value = ElementRequest::class, name = "ElementRequest"),
    JsonSubTypes.Type(value = WorkspaceRequest::class, name = "WorkspaceRequest"),
    JsonSubTypes.Type(value = NamespaceRequest::class, name = "NamespaceRequest"),
    JsonSubTypes.Type(value = BookmarkRequest::class, name = "BookmarkRequest"),
    JsonSubTypes.Type(value = UserPreferenceRequest::class, name = "UserPreferenceRequest"),
)
interface WDRequest
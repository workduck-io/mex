package com.serverless.eventUtils

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "itemType",
        //defaultImpl = GenericListRequest::class
)
@JsonSubTypes(
        JsonSubTypes.Type(value = NodeImage::class, name = "Node"),
)
interface Image{
}
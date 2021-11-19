package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.models.AdvancedElement
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.WorkspaceIdentifier

class NodeRequest(

        @JsonProperty("type")
       var type : String?,

        @JsonProperty("lastEditedBy")
        val lastEditedBy : String,

        @JsonProperty("id")
        val id : String,

        @JsonProperty("namespaceIdentifier")
        @JsonSerialize(converter = IdentifierSerializer::class)
        @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
        val namespaceIdentifier: NamespaceIdentifier?= null,

        @JsonProperty("workspaceIdentifier")
        @JsonSerialize(converter = IdentifierSerializer::class)
        @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
        val workspaceIdentifier: WorkspaceIdentifier?= null,

        @JsonProperty("data")
        val data: List<AdvancedElement>,

        )  {


}

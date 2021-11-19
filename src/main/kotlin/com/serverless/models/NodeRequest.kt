package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.models.AdvancedElement
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.WorkspaceIdentifier

class NodeRequest(

        @JsonProperty("type")
        private var type : String,

        @JsonProperty("lastEditedBy")
        val lastEditedBy : String,

        @JsonProperty("id")
        val id : String,

        @JsonProperty("namespaceIdentifier")
        val namespaceIdentifier: NamespaceIdentifier?= null,

        @JsonProperty("workspaceIdentifier")
        val workspaceIdentifier: WorkspaceIdentifier?= null,

        @JsonProperty("data")
        val data: List<AdvancedElement>,

) : WDRequest {

        override fun getType() : String = type

}

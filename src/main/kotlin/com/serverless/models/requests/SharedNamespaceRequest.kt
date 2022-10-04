package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
import com.workduck.converters.AccessTypeDeserializer
import com.workduck.models.AccessType


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SharedNamespaceRequest")
data class SharedNamespaceRequest(

        val namespaceID: String,

        val userIDs: List<String> = listOf(),

        @JsonDeserialize(converter = AccessTypeDeserializer::class)
        val accessType: AccessType = AccessType.MANAGE

) : WDRequest {

    init {
        require(userIDs.isNotEmpty()) { "Need to provide userIDs" }

        require(namespaceID.isValidID(Constants.NAMESPACE_ID_PREFIX)) { "Invalid NamespaceID" }

    }
}
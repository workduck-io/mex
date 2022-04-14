package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.SnippetIdentifier

class SnippetIdentifierConverter : DynamoDBTypeConverter<String, SnippetIdentifier?> {

    override fun convert(n: SnippetIdentifier?): String? {
        return n?.id
    }

    override fun unconvert(snippetID: String?): SnippetIdentifier? {
        return snippetID?.let { SnippetIdentifier(it) }
    }
}

package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.HierarchyUpdateSource

class HierarchyUpdateSourceConverter : DynamoDBTypeConverter<String, HierarchyUpdateSource> {

    override fun convert(source: HierarchyUpdateSource): String {
        return source.name
    }

    override fun unconvert(source: String): HierarchyUpdateSource {
        return HierarchyUpdateSource.valueOf(source)
    }
}

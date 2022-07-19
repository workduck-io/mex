package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.AdvancedElementProperties

class AdvancedElementPropertiesSerializer : StdConverter<Map<AdvancedElementProperties, Any>, Map<String, Any>>() {
    override fun convert(propertiesMap: Map<AdvancedElementProperties, Any>): Map<String, Any> {

        val mapOfPropertiesSerialized: MutableMap<String, Any> = mutableMapOf()
        for ((propertyEnum, propertyValue) in propertiesMap) {
            mapOfPropertiesSerialized[propertyEnum.getType()] = propertyValue
        }
        return mapOfPropertiesSerialized
    }
}


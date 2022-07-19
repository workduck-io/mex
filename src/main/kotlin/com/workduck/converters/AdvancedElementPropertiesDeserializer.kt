package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElementProperties
import com.workduck.utils.Helper

class AdvancedElementPropertiesDeserializer: StdConverter<Map<String, Any>, Map<AdvancedElementProperties, Any>>() {


    override fun convert(propertiesMap: Map<String, Any>): Map<AdvancedElementProperties, Any> {

        val mapOfPropertiesDeserialized: MutableMap<AdvancedElementProperties, Any> = mutableMapOf()
        for ((propertyName, propertyValue) in propertiesMap) {
            val enum = AdvancedElementProperties.fromName(propertyName) ?: throw IllegalArgumentException("Invalid Property $propertyName")
            mapOfPropertiesDeserialized[enum] = propertyValue
        }
        return mapOfPropertiesDeserialized

    }
}

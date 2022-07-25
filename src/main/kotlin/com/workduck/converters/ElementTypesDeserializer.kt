package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.ElementTypes

class ElementTypesDeserializer : StdConverter<String, ElementTypes>() {
    override fun convert(elementType: String): ElementTypes {
        return ElementTypes.fromName(elementType) ?: throw IllegalArgumentException("Invalid Element Type $elementType")
    }
}

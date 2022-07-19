package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.ElementTypes

class ElementTypesDeserializer : StdConverter<String, ElementTypes>() {
    override fun convert(accessType: String): ElementTypes {
        return ElementTypes.fromName(accessType) ?: throw IllegalArgumentException("Invalid Element Type")
    }
}

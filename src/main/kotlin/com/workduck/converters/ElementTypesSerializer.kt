package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.ElementTypes

class ElementTypesSerializer: StdConverter<ElementTypes, String>() {
    override fun convert(elementType: ElementTypes): String {
        return elementType.getType()
    }
}


package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.Identifier
import com.workduck.models.NamespaceIdentifier

class IdentifierSerializer : StdConverter<Identifier?, String?>() {
    override fun convert(value: Identifier?): String? {
        return value?.id
    }

}

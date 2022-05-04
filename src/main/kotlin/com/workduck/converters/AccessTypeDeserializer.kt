package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.AccessType

class AccessTypeDeserializer : StdConverter<String, AccessType>() {
    override fun convert(accessType: String): AccessType {
        return AccessType.valueOf(accessType.uppercase())
    }
}
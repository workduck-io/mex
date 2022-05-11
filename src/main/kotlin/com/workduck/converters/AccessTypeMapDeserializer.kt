package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.AccessType

class AccessTypeMapDeserializer  : StdConverter<Map<String, String>, MutableMap<String, AccessType>>() {
    override fun convert(accessTypeMap: Map<String, String>): MutableMap<String, AccessType> {
        val objectMap = mutableMapOf<String, AccessType>()
        for((userID, accessType) in accessTypeMap){
            objectMap[userID] = AccessType.valueOf(accessType)
        }
        return objectMap
    }
}
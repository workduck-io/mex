package com.workduck.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElement
import com.workduck.utils.Helper
import java.io.IOException

class NodeRequestDataDeserializer : JsonDeserializer<MutableMap<String, AdvancedElement>>() {

    val objectMapper = Helper.objectMapper

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): MutableMap<String, AdvancedElement> {

        val oc: ObjectCodec = jp.codec
        val objMap: JsonNode = oc.readTree(jp)

        val map = mutableMapOf<String, AdvancedElement>()
        for(n in objMap){
          val element : AdvancedElement = objectMapper.readValue(objectMapper.writeValueAsString(n))
          map[element.id] = element
        }

        return map
    }
}

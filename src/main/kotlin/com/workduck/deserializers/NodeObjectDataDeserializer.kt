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


/* Used specifically for DDB streams use case when converting a map json to map object */
class NodeObjectDataDeserializer : JsonDeserializer<MutableMap<String, AdvancedElement>>() {

    val objectMapper = Helper.objectMapper

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): MutableMap<String, AdvancedElement> {

        val oc: ObjectCodec = jp.codec
        val objMap: JsonNode = oc.readTree(jp)

        val map = mutableMapOf<String, AdvancedElement>()
        for( n in objMap){
            var string = n.textValue()
            val element : AdvancedElement = objectMapper.readValue(string)
            map[element.id] = element
        }

        return map
    }
}

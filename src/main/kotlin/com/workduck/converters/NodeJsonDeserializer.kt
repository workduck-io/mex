package com.workduck.converters

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.workduck.models.Node
import java.io.IOException

class NodeJsonDeserializer : JsonDeserializer<Node?>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Node {
        println("Helloooo")
        val oc: ObjectCodec = jp.codec
        val objMap: JsonNode = oc.readTree(jp)
        val node: Node = Node()
        node.id = objMap["id"].toString()
        // node.

        return Node("Testing")
    }
}

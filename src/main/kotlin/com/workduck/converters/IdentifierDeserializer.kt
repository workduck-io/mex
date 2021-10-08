package com.workduck.converters
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.Identifier
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.Node
import com.workduck.models.WorkspaceIdentifier
import java.io.IOException


class IdentifierDeserializer : StdConverter<String?, Identifier?>(){
    override fun convert(value: String?): Identifier? {
        return if (value != null) {
            if(value.startsWith("NAMESPACE")){
                NamespaceIdentifier(value)
            } else{
                WorkspaceIdentifier(value)
            }

        } else null
    }

}

class NamespaceIdentifierDeserializer : StdConverter<String?, NamespaceIdentifier?>() {
    override fun convert(value: String?): NamespaceIdentifier? {
        return if (value != null)
            NamespaceIdentifier(value)
        else null
    }

}


class WorkspaceIdentifierDeserializer : StdConverter<String?, WorkspaceIdentifier?>() {
    override fun convert(value: String?): WorkspaceIdentifier? {
        return if (value != null)
            WorkspaceIdentifier(value)
        else null
    }

}




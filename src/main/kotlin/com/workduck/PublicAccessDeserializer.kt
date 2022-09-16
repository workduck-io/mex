package com.workduck

import com.fasterxml.jackson.databind.util.StdConverter

class PublicAccessDeserializer : StdConverter<String, Boolean>() {

    override fun convert(publicAccessValue: String): Boolean {
        return publicAccessValue == "1"
    }


}
package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter

class PublicAccessSerializer: StdConverter<Boolean, String>() {

    override fun convert(publicAccessValue: Boolean): String {
        return if(publicAccessValue) "1"
        else "0"
    }


}
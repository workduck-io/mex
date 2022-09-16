package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.ItemType

class ItemTypeDeserializer : StdConverter<String, ItemType>() {
    override fun convert(itemTypeString: String): ItemType {
        return ItemType.valueOf(itemTypeString)
    }
}


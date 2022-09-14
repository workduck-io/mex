package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.ItemType

class ItemTypeSerializer : StdConverter<ItemType, String>() {
    override fun convert(ietmTypeEnum: ItemType): String {
        return ietmTypeEnum.name
    }
}

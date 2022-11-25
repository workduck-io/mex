package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.BlockMovementAction

class BlockMovementActionDeserializer : StdConverter<String, BlockMovementAction>() {
    override fun convert(action: String): BlockMovementAction {
        return BlockMovementAction.valueOf(action.uppercase())
    }
}

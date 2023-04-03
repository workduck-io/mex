package com.workduck.utils

import com.serverless.models.requests.EntityRequest
import com.workduck.models.AdvancedElement

object EntityHelper {

    fun createEntityReferenceBlock(blockID: String, entityID : String, elementType : String) : AdvancedElement{
        return AdvancedElement(id = blockID, entityRefID = entityID, elementType = elementType)
    }

    fun createEntityPayload(entity : AdvancedElement) : EntityRequest {
        return EntityRequest(entity)
    }
}
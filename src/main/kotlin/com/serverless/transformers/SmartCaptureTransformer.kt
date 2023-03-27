package com.serverless.transformers

import com.serverless.models.responses.Response
import com.serverless.models.responses.SmartCaptureResponse
import com.workduck.models.AdvancedElement
import com.workduck.models.BlockElement
import com.workduck.models.CaptureEntity
import com.workduck.models.SmartCapture
import com.workduck.models.WorkspaceIdentifier

class SmartCaptureTransformer:Transformer<CaptureEntity>, BulkTransformer<CaptureEntity> {
    override fun transform(t: List<CaptureEntity>): List<Response> = t.let {
        val responses = mutableListOf<SmartCaptureResponse>()
        for (captureEntity in it) {
            responses.add(this.transform(captureEntity) as SmartCaptureResponse)
        }
        return responses
    }

    // TODO(re-review this )
    override fun transform(t: CaptureEntity?): Response? = t?.let {
        val smartCapture = SmartCapture()
        val advancedElements = mutableListOf<AdvancedElement>()
        smartCapture.id = t.captureID
        smartCapture.title = t.page ?: ""
        smartCapture.workspaceIdentifier = WorkspaceIdentifier(t.workspaceID ?: "")
        smartCapture.createdBy = t.userID
        //TODO(fix this)
        smartCapture.data = listOf(BlockElement(captureID = t.captureID, configID = t.configID, blockID = ""))

        for (data in t.data!!) {
            val childAdvancedElements = mutableListOf<AdvancedElement>()
            childAdvancedElements.add(
                AdvancedElement(
                    id = data.id.toString(),
                    elementType = "p",
                    properties = data.properties,
                    children = mutableListOf(
                        AdvancedElement(
                            properties = mapOf(
                                "label" to data.label.toString(),
                                "value" to data.value.toString()
                            )
                        )
                    )
                )
            )
            advancedElements.add(
                AdvancedElement(children = childAdvancedElements)
            )
        }

        SmartCaptureResponse(
            id = smartCapture.id,
            captureReference = smartCapture.data,
            content = advancedElements,
            title = smartCapture.title,
            lastEditedBy = smartCapture.lastEditedBy,
            createdBy = smartCapture.createdBy,
            createdAt = smartCapture.createdAt,
            updatedAt = smartCapture.updatedAt,
            version = smartCapture.version,
            template = smartCapture.template,
            metadata = smartCapture.metadata
        )
    }
}
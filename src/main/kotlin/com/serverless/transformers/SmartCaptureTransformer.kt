package com.serverless.transformers

import com.serverless.models.responses.CaptureEntity
import com.serverless.models.responses.Response
import com.serverless.models.responses.SmartCaptureResponse
import com.workduck.models.AdvancedElement
import com.workduck.models.BlockElement
import com.workduck.models.SmartCapture
import com.workduck.models.WorkspaceIdentifier

class SmartCaptureTransformer:Transformer<CaptureEntity> {
    override fun transform(t: CaptureEntity?): Response? = t?.let {
        val smartCapture = SmartCapture()
        val advancedElements = mutableListOf<AdvancedElement>()
        smartCapture.id = t.captureId.toString()
        smartCapture.title = t.page.toString()
        smartCapture.workspaceIdentifier = WorkspaceIdentifier(t.workspaceId.toString())
        smartCapture.createdBy = t.userId
        smartCapture.data = listOf(BlockElement(captureId = t.captureId.toString(), configId = t.configId.toString()))

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
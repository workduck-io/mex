package com.serverless.utils

import com.serverless.models.requests.SmartCaptureRequest
import com.serverless.models.responses.Response
import com.serverless.transformers.SmartCaptureTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.*

object SmartCaptureHelper {
    val smartCaptureTransformer : Transformer<CaptureEntity> = SmartCaptureTransformer()

    fun convertSmartCaptureToSmartCaptureResponse(capture: CaptureEntity?) : Response? {
        return smartCaptureTransformer.transform(capture)
    }

    fun convertAdvancedToBlockElement(request: SmartCaptureRequest): List<BlockElement> {
        val blockElement = mutableListOf<BlockElement>()

        for (data in request.data) {
            blockElement.add(BlockElement(captureId = request.id, configId = data.properties?.get("configId") as String))
        }
        return blockElement
    }

    fun serializeRequestToEntity(request: SmartCaptureRequest): CaptureEntity{
        val captureEntity = CaptureEntity()
        val captureElements = mutableListOf<CaptureElements>()
        // Assuming there will be only one element
        val data = request.data[0]
        captureEntity.captureId = request.id
        captureEntity.configId = data.properties?.get("configId") as String?
        captureEntity.page = data.properties?.get("page") as String?
        captureEntity.source = data.properties?.get("source") as String?

        for (child in data.children!!) {
            captureElements.add(
                CaptureElements(
                    id = child.id,
                    label = child.children?.get(0)?.properties?.get("label") as String?,
                    value = child.children?.get(0)?.properties?.get("value") as String?,
                    properties = child.properties
                )
            )
        }
        captureEntity.data = captureElements
        return captureEntity
    }
}


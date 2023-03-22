package com.serverless.utils

import com.serverless.models.requests.SmartCaptureRequest
import com.serverless.models.responses.Response
import com.serverless.transformers.SmartCaptureTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.BlockElement
import com.workduck.models.CaptureElements
import com.workduck.models.CaptureEntity

object SmartCaptureHelper {
    val smartCaptureTransformer : Transformer<CaptureEntity> = SmartCaptureTransformer()

    fun convertSmartCaptureToSmartCaptureResponse(capture: CaptureEntity?) : Response? {
        return smartCaptureTransformer.transform(capture)
    }

    fun convertAdvancedToBlockElement(request: SmartCaptureRequest): List<BlockElement> {
        val blockElementList = mutableListOf<BlockElement>()

        for (blockData in request.data) {
            val configID = blockData.properties?.get("configID") as String? ?: throw IllegalStateException("ConfigID not found")
            blockElementList.add(
                BlockElement(captureId = request.id, configId = configID)
            )
        }
        return blockElementList
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

        // Only one level of nesting??
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


package com.serverless.utils

import com.serverless.models.requests.SmartCaptureRequest
import com.serverless.models.responses.Response
import com.serverless.transformers.SmartCaptureTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.BlockElement
import com.workduck.models.CaptureElements
import com.workduck.models.CaptureEntity

object SmartCaptureHelper {
    val smartCaptureTransformer = SmartCaptureTransformer()

    fun convertSmartCaptureToSmartCaptureResponse(capture: CaptureEntity?) : Response? {
        return smartCaptureTransformer.transform(capture)
    }

    fun convertSmartCaptureToSmartCaptureArrayResponse(capture: Array<CaptureEntity>?): Array<Response> {
        return  smartCaptureTransformer.transform(capture!!)
    }

    fun convertAdvancedToBlockElement(request: SmartCaptureRequest): List<BlockElement> {
        val blockElementList = mutableListOf<BlockElement>()

        for (blockData in request.data) {
            val configID = blockData.properties?.get("configID") as String? ?: throw IllegalStateException("ConfigID not found")
            blockElementList.add(
                BlockElement(blockID = blockData.id, captureID = request.id, configID = configID)
            )
        }
        return blockElementList
    }

    fun serializeRequestToEntity(request: SmartCaptureRequest): CaptureEntity{
        val captureEntity = CaptureEntity(captureID = request.id, configID = request.data.first().properties?.get("configID") as String)
        val captureElements = mutableListOf<CaptureElements>()

        // Assuming there will be only one element
        for ( data in request.data ) {
            captureEntity.captureID = request.id
            captureEntity.configID = data.properties?.get("configId") as String
            captureEntity.page = data.properties?.get("page") as String
            captureEntity.source = data.properties?.get("source") as String

            for (child in data.children!!) {
                captureElements.add(
                    CaptureElements(
                        id = child.id,
                        label = child.children?.get(0)?.properties?.get("label") as String,
                        value = child.children?.get(0)?.properties?.get("value") as String,
                        properties = child.properties
                    )
                )
            }
            captureEntity.data = captureElements
        }
        return captureEntity
    }
}


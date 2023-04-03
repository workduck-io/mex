package com.serverless.utils

import com.serverless.models.responses.Response
import com.serverless.transformers.SmartCaptureTransformer
import com.workduck.models.CaptureEntity

object SmartCaptureHelper {
    val smartCaptureTransformer = SmartCaptureTransformer()

    fun convertSmartCaptureToSmartCaptureArrayResponse(capture: List<CaptureEntity>?): List<Response> {
        return  smartCaptureTransformer.transform(capture!!)
    }

}


package com.serverless.transformers

import com.serverless.models.responses.Response
import com.serverless.models.responses.SmartCaptureResponse
import com.workduck.models.AdvancedElement
import com.workduck.models.BlockElement
import com.workduck.models.CaptureEntity
import com.workduck.models.SmartCapture
import com.workduck.models.WorkspaceIdentifier

class SmartCaptureTransformer:Transformer<CaptureEntity>, BulkTransformer<CaptureEntity> {

    override fun transform(t: CaptureEntity?): Response? {
        TODO("Not yet implemented")
    }

    override fun transform(t: List<CaptureEntity>): List<Response> {
        TODO("Not yet implemented")
    }

}
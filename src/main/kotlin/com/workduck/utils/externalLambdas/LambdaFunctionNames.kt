package com.workduck.utils.externalLambdas

object LambdaFunctionNames {
    val stage: String = System.getenv("STAGE") ?: "test"
    val CAPTURE_LAMBDA = "smartcapture-$stage-capture"
    val HIGHLIGHT_LAMBDA = "highlights-$stage-main"
}
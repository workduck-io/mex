package com.workduck.utils.externalLambdas

object RoutePaths {
    const val CREATE_CAPTURE = "/capture"
    const val GET_CAPTURE = "/config/{configId}/capture/{captureId}"
    const val GET_ALL_CAPTURES_WITH_CONFIGID = "/config/{configId}/all"
    const val GET_ALL_CAPTURES_FOR_USER = "/config/{configId}/captures"
    const val DELETE_CAPTURE = "/config/{configId}/capture/{captureId}"
}

object HttpMethods {
    const val POST = "POST"
    const val GET = "GET"
    const val PUT = "PUT"
    const val PATCH = "PATCH"
    const val DELETE = "DELETE"
}
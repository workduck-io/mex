package com.workduck.utils.externalLambdas

object RoutePaths {
    const val CREATE_CAPTURE = "/capture"
    const val GET_CAPTURE = "/capture/{captureId}"
    const val GET_ALL_CAPTURES_WITH_FILTER = "/capture"
    const val DELETE_CAPTURE = "/capture/{captureId}"
}

object HttpMethods {
    const val POST = "POST"
    const val GET = "GET"
    const val PUT = "PUT"
    const val PATCH = "PATCH"
    const val DELETE = "DELETE"
}
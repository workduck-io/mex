package com.workduck.utils.externalLambdas

object RoutePaths {
    const val CREATE_CAPTURE = "/capture"
    const val UPDATE_CAPTURE = "/capture/{id}"
    const val GET_CAPTURE = "/capture/{id}"
    const val GET_ALL_CAPTURES_WITH_FILTER = "/capture/all"
    const val DELETE_CAPTURE = "/capture/{id}"
}

object HttpMethods {
    const val POST = "POST"
    const val GET = "GET"
    const val PUT = "PUT"
    const val PATCH = "PATCH"
    const val DELETE = "DELETE"
}
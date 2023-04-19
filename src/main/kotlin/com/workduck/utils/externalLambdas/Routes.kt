package com.workduck.utils.externalLambdas

object RoutePaths {
    const val CREATE_CAPTURE = "/"
    const val UPDATE_CAPTURE = "/{id}"
    const val GET_CAPTURE = "/{id}"
    const val GET_ALL_CAPTURES_WITH_FILTER = "/all"
    const val DELETE_CAPTURE = "/{id}"
    const val CREATE_HIGHLIGHT = "/"
    const val GET_HIGHLIGHT = "/{id}"
    const val DELETE_HIGHLIGHT = "/{id}"
    const val GET_ALL_HIGHLIGHTS = "/all"
    const val GET_MULTIPLE_HIGHLIGHTS = "/multiple"
}

object HttpMethods {
    const val POST = "POST"
    const val GET = "GET"
    const val PUT = "PUT"
    const val PATCH = "PATCH"
    const val DELETE = "DELETE"
}
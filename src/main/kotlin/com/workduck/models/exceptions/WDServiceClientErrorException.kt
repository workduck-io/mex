package com.workduck.models.exceptions


class WDServiceClientErrorException(val statusCode: Int, message: String) : Exception(message)

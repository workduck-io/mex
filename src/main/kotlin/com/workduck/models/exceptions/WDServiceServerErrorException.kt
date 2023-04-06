package com.workduck.models.exceptions

class WDServiceServerErrorException(val statusCode: Int, message: String) : Exception(message)

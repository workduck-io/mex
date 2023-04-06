package com.workduck.utils

import com.serverless.utils.Constants

object SmartCaptureHelper
{
    fun getSmartCaptureSK(captureID: String, version: Int) : String = "$captureID${Constants.DELIMITER}$version"
}
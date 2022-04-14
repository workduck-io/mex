package com.workduck.utils

import com.serverless.utils.Constants

object SnippetHelper {

    fun getSnippetSK(snippetID: String, version: Long) : String = "$snippetID${Constants.DELIMITER}$version"
}
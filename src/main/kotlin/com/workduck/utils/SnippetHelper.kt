package com.workduck.utils

import com.serverless.utils.Constants

object SnippetHelper {

    fun getSnippetSK(snippetID: String, version: Int) : String = "$snippetID${Constants.DELIMITER}$version"

    fun isCreatePossible(version: Int): Boolean{
        return version == 1
    }

}
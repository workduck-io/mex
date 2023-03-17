package com.serverless.utils

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.google.gson.Gson
import com.serverless.models.Input
import com.workduck.utils.Helper
import java.util.regex.Pattern

object Helper {

    fun validateTokenAndWorkspace(input: Input){
        if(!Helper.validateWorkspace(input.headers.workspaceID, input.tokenBody.workspaceIDList)){
            throw UnauthorizedException("Not Authorized for the requested workspace")
        }

    }

    val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    )
}
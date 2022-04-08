package com.serverless.utils

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.serverless.models.Input
import com.serverless.models.TokenBody
import com.workduck.utils.Helper

object Helper {

    fun validateTokenAndWorkspace(input: Input){
        if(!Helper.validateWorkspace(input.headers.workspaceID, input.tokenBody.workspaceIDList)){
            throw UnauthorizedException("Not Authorized for the requested workspace")
        }

    }
}
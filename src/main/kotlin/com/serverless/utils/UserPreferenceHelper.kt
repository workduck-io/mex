package com.serverless.utils

import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.serverless.transformers.UserPreferenceTransformer
import com.workduck.models.UserPreferenceRecord

object UserPreferenceHelper {

    val userPreferenceTransformer : Transformer<UserPreferenceRecord> = UserPreferenceTransformer()


    fun convertUserPreferenceRecordToUserPreferenceResponse(userPreferenceRecord: UserPreferenceRecord?) : Response? {
        return userPreferenceTransformer.transform(userPreferenceRecord)
    }
}
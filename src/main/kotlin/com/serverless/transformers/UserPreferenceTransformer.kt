package com.serverless.transformers

import com.serverless.models.Response
import com.serverless.models.UserPreferenceResponse
import com.workduck.models.UserPreferenceRecord

class UserPreferenceTransformer : Transformer<UserPreferenceRecord> {
    override fun transform(t: UserPreferenceRecord?): Response? = t?.let {
       UserPreferenceResponse(
                userID = t.userID,
                preferenceType = t.preferenceType,
                preferenceValue = t.preferenceValue
       )
    }
}
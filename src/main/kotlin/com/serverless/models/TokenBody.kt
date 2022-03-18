package com.serverless.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenBody(
    val sub: String,
    @JsonProperty("cognito:username")
    val userName: String,
    val iss: String,
    @JsonProperty("custom:mex_workspace_ids")
    val workspaceIDs: String?,
    val email: String,
) {
    val workspaceIDList: List<String> = workspaceIDs?.split("#") ?: listOf()
    val userPoolID: String = iss.split("/").last()

    companion object {
        fun fromToken(bearerToken : String) : TokenBody?{
            return try{
                val encodedToken = bearerToken.split(" ")[1]
                val decodedString = String(Base64.getDecoder().decode(encodedToken.split(".")[1]))
                Helper.objectMapper.readValue<TokenBody>(decodedString)
            }catch (e : Exception){
                LOG.info(e)
                null
            }
        }


        private val LOG = LogManager.getLogger(TokenBody::class.java)
    }
}

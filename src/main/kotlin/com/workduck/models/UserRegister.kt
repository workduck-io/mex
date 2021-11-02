package com.workduck.models

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRegister(

	@JsonProperty("user")
	val user : User? = null,

	@JsonProperty("workspaceName")
	val workspaceName: String? = null
) {

}
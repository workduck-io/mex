package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BookmarkRequest(

) : WDRequest
package com.serverless.sqsEventHandlers

import com.workduck.utils.Helper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.eventUtils.Image

object EventHelper {

   val objectMapper = Helper.objectMapper
   fun getImageObjectFromImage(imageString : String) : Image {
      return objectMapper.readValue(imageString)
   }
}
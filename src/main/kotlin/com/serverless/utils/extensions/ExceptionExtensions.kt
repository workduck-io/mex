package com.serverless.utils.extensions

import com.workduck.models.CaptureEntity
import com.workduck.models.Entity
import com.workduck.models.exceptions.WDNotFoundException



fun Entity?.withNotFoundException() : Entity = this ?: throw WDNotFoundException("Requested Entity Not Found")

fun CaptureEntity?.withNotFoundException() : CaptureEntity = this ?: throw WDNotFoundException("Requested Capture Not Found")

fun Array<CaptureEntity>?.withNotFoundException() : Array<CaptureEntity> = this ?: throw WDNotFoundException("Requested Captures Not Found")

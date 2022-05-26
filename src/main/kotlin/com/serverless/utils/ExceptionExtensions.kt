package com.serverless.utils

import com.workduck.models.Entity
import com.workduck.models.exceptions.WDNotFoundException



fun Entity?.withNotFoundException() : Entity = this ?: throw WDNotFoundException("Requested Entity Not Found")

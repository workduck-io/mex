package com.workduck.models


enum class ItemStatus {
    ACTIVE,
    ARCHIVED
}


interface ItemStatusAdherence {
    val itemStatus : ItemStatus
}
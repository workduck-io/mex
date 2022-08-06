package com.workduck.models

import com.serverless.utils.Constants
import com.workduck.utils.Helper

class User(

    var id: String = Helper.generateId(IdentifierType.USER.name),

    var name: String? = null,

    var alias: String? = null,


    var email: String? = null,

    override var itemType: ItemType = ItemType.User,

    var createdAt: Long? = Constants.getCurrentTime()
) : Entity {

    var updatedAt: Long = Constants.getCurrentTime()
}

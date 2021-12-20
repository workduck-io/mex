package com.serverless.eventUtils

class ActionFactory {

    companion object{
        const val modify = "MODIFY"

        const val remove = "REMOVE"

        const val insert = "INSERT"

        private val actionRegistry : Map<String, Action> = mapOf(
            modify to Modify(),
            remove to Remove(),
            insert to Insert()
        )

        fun getAction(action : String) : Action? {
            return actionRegistry[action]
        }


    }
}
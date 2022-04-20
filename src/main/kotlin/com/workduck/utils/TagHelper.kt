package com.workduck.utils

import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.workduck.models.Tag
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object TagHelper {

    fun convertObjectToTag(tagObject : Any) : Tag {
        return Helper.objectMapper.readValue(Helper.objectMapper.writeValueAsString(tagObject))
    }

    fun getNodesMapFromOutcomeObject(newValues: UpdateItemOutcome) : HashMap<String, String> {
        return Helper.objectMapper.readValue(Helper.objectMapper.writeValueAsString(newValues.item["nodes"]))
    }

    fun createTags(tagNameList: MutableList<String>?, nodeID: String, workspaceID: String){
        if(!tagNameList.isNullOrEmpty()){
            callCreateTagLambda(tagNameList, nodeID, workspaceID)
        }
    }

    fun deleteTags(tagNameList: MutableList<String>?, nodeID: String, workspaceID: String){
        if(!tagNameList.isNullOrEmpty()){
            callDeleteTagLambda(tagNameList, nodeID, workspaceID)
        }
    }

    fun updateTags(newTagNameList: MutableList<String>?, storedTagNameList: MutableList<String>?, nodeID: String, workspaceID: String) = runBlocking{
        val addedTags = getDifferenceOfTags(newTagNameList, storedTagNameList)
        val deletedTags = getDifferenceOfTags(storedTagNameList, newTagNameList)

        if(addedTags.isNotEmpty()) launch {  callCreateTagLambda(addedTags, nodeID, workspaceID) }
        if(deletedTags.isNotEmpty()) launch {  callDeleteTagLambda(deletedTags, nodeID, workspaceID) }

    }



    private fun getDifferenceOfTags(firstList: MutableList<String>?, secondList: List<String>?) : List<String>{
        return if ( bothListsNullOrEmpty(firstList, secondList) ) return listOf()
        else if ( firstList.isNullOrEmpty() ) return listOf()
        else if ( secondList.isNullOrEmpty() ) return firstList
        else firstList.minus(secondList)
    }

    private fun bothListsNullOrEmpty(firstList: MutableList<String>?, secondList: List<String>?) : Boolean{
        return firstList.isNullOrEmpty() && secondList.isNullOrEmpty()
    }

    private fun callCreateTagLambda(tagNameList: List<String>, nodeID: String, workspaceID: String) {
        val payload = generatePayload(tagNameList, nodeID, workspaceID, "POST /tag")
        callTagLambdaWithPayload(payload)
    }

    private fun callDeleteTagLambda(tagNameList: List<String>, nodeID: String, workspaceID: String) {
        val payload = generatePayload(tagNameList, nodeID, workspaceID, "DELETE /tag")
        callTagLambdaWithPayload(payload)
    }

    private fun callTagLambdaWithPayload(payload: String){
        val lambdaClient = AWSLambdaClient.builder().withRegion("us-east-1").build()

        val stage = System.getenv("STAGE") ?: "local"
        val functionName = "mex-backend-$stage-InternalTag"

        lambdaClient.invoke(InvokeRequest().withFunctionName(functionName).withPayload(payload))
    }

    private fun generatePayload(tagNameList: List<String>, nodeID: String, workspaceID: String, routeKey: String) : String {


        val body = """
            {
               "type" : "TagRequest",
               "tagNames" : ${Gson().toJson(tagNameList)},
               "nodeID" : "$nodeID"
            }
        """

        return """
            {
                "routeKey" : "$routeKey",
                "workspaceID" : "$workspaceID" ,
                "body" : $body
            }
        """
    }

}
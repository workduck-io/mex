package com.workduck.utils

import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Tag

object TagHelper {

    fun convertObjectToTag(tagObject : Any) : Tag {
        return Helper.objectMapper.readValue(Helper.objectMapper.writeValueAsString(tagObject))
    }

    fun getNodesMapFromOutcomeObject(newValues: UpdateItemOutcome) : HashMap<String, String> {
        return Helper.objectMapper.readValue(Helper.objectMapper.writeValueAsString(newValues.item["nodes"]))
    }

    fun createTags(tagNameList: MutableList<String>?, nodeID: String){
        if(!tagNameList.isNullOrEmpty()){
            callCreateTagLambda(tagNameList, nodeID)
        }
    }

    fun updateTags(newTagNameList: MutableList<String>?, storedTagNameList: MutableList<String>?, nodeID: String){
        val addedTags = getDifferenceOfTags(newTagNameList, storedTagNameList)
        val deletedTags = getDifferenceOfTags(storedTagNameList, newTagNameList)



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

    private fun callCreateTagLambda(tagNameList: List<String>, nodeID: String) {
        val payLoad = generatePayload(tagNameList, nodeID)


    }

    private fun callDeleteTagLambda(tagNameList: List<String>, nodeID: String) {

    }

}
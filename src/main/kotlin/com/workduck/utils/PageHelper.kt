package com.workduck.utils

import com.workduck.models.Page

object PageHelper {

    fun orderBlocks(page: Page): Page =
            page.apply {
                page.data?.let { data ->
                    (
                            page.dataOrder?.mapNotNull { blockId ->
                                data.find { element -> blockId == element.id }
                            } ?: emptyList()
                            )
                            .also {
                                page.data = it.toMutableList()
                            }
                }
            }

    fun createDataOrderForPage(page: Page): MutableList<String> {

        val list = mutableListOf<String>()
        if(page.data.isNullOrEmpty()) return mutableListOf()
        for (element in page.data!!) {
            list += element.id
        }
        return list
    }

    /* If a block has been changed, change its metadata as well */
    /* If the page has changed, return true else false */
    fun comparePageDataWithStoredPage(page: Page, storedPage: Page) : Boolean{
        var pageChanged = false

        /* in case a block has been deleted */
        if(page.data != storedPage.data || page.title != storedPage.title) pageChanged = true

        if (page.data != null) {
            for (currElement in page.data!!) {
                var isPresent = false
                if(storedPage.data != null) {
                    for (storedElement in storedPage.data!!) {
                        if (storedElement.id == currElement.id) {
                            isPresent = true

                            /* if the block has not been updated */
                            if (currElement == storedElement) {
                                currElement.createdAt = storedElement.createdAt
                                currElement.updatedAt = storedElement.updatedAt
                                currElement.createdBy = storedElement.createdBy
                                currElement.lastEditedBy = storedElement.lastEditedBy
                            }

                            /* when the block has been updated */
                            else {
                                pageChanged = true
                                currElement.createdAt = storedElement.createdAt
                                currElement.updatedAt = System.currentTimeMillis()
                                currElement.createdBy = storedElement.createdBy
                                currElement.lastEditedBy = page.lastEditedBy
                            }
                        }
                    }

                    if (!isPresent) {
                        pageChanged = true
                        currElement.createdAt = page.updatedAt
                        currElement.updatedAt = page.updatedAt
                        currElement.createdBy = page.lastEditedBy
                        currElement.lastEditedBy = page.lastEditedBy
                    }
                }

            }
        }
        return pageChanged
    }


    fun mergePageVersions(page: Page, storedPage: Page) {

        page.version = storedPage.version

        /*
        /* if the same user edited the node the last time, he can overwrite anything */
        if(page.lastEditedBy == storedPage.lastEditedBy){
            page.version = storedPage.version
            return
        }
        /* currently just handling when more blocks have been added */

        /* not handling the case when
            1. same block(s) has/have been edited
            2. some blocks deleted either by user1 or user2
        */
        val storedPageDataOrder = storedPage.dataOrder
        val sentDataOrder = page.dataOrder
        val finalDataOrder = mutableListOf<String>()

        //very basic handling of maintaining rough order amongst blocks
        if(storedPageDataOrder != null && sentDataOrder != null) {

            for(storedNodeID in storedPageDataOrder){
                finalDataOrder.add(storedNodeID)
            }

            for (storedPageID in storedPageDataOrder) {
                for(sentPageID in sentDataOrder) {
                    if (storedPageID == sentPageID && storedPageID !in finalDataOrder) {
                        finalDataOrder.add(storedPageID)
                    }
                }
            }

            for(sentNodeID in sentDataOrder){
                if(sentNodeID !in finalDataOrder) finalDataOrder.add(sentNodeID)
            }
        }

        page.dataOrder = finalDataOrder
        page.version = storedPage.version */

        // TODO(explore autoMerge cmd line)
    }

}
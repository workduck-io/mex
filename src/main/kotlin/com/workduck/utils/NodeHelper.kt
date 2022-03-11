package com.workduck.utils

import com.workduck.utils.Helper.commonPrefixList
import com.workduck.utils.Helper.splitIgnoreEmpty
import org.apache.logging.log4j.LogManager

object NodeHelper {

    private val LOG = LogManager.getLogger(NodeHelper::class.java)

    /* returns path of the format nodeName#nodeID#nodeName#nodeID ... */
    fun getLongestExistingPath(nodeHierarchyInformation: List<String>?, nodeNamePath: String): String {

        var longestExistingPath = ""
        nodeHierarchyInformation?.let {
            for (existingNodePath in nodeHierarchyInformation) {
                val longestCommonNamePath = getCommonPrefixNodePath(nodeNamePath, existingNodePath)

                LOG.info(longestCommonNamePath)
                if(longestCommonNamePath != ""){
                    val nodeNamesAndIDs = existingNodePath.split("#")
                    val commonNodeNamesAndIDs = nodeNamesAndIDs.subList(0, 2*longestCommonNamePath.splitIgnoreEmpty("#").size)

                    if(commonNodeNamesAndIDs.size > longestExistingPath.split("#").size)
                        longestExistingPath = commonNodeNamesAndIDs.joinToString("#")
                    /*
                    Longest Name Path List = [A, B] => We need node name and id information of first two nodes from existing path
                    nodeNamesAndIDs = [A, Aid, B, Bid, C, Cid, D, Did ...]
                    We need sublist from 0 to 3

                    After that we just check if nodes in current common path > longestPath yet
                     */
                }

            }
        }
        LOG.info(longestExistingPath)
        return longestExistingPath
    }

    fun getCommonPrefixNodePath(path1: String, path2: String): String{
        return path1.split("#").commonPrefixList(path2.split("#")).joinToString("#")
    }

    /* nodePath is of the format : node1Name#node1ID#node2Name#node2ID.. */
    fun getNamePath(nodePath: String): String {
        val nodeNames = mutableListOf<String>()
        nodePath.split("#").mapIndexed {
            index, string ->
            if (index % 2 == 0) nodeNames.add(string)
        }
        return nodeNames.joinToString("#")
    }

    fun getIDPath(nodePath: String): String {
        val nodeNames = mutableListOf<String>()
        nodePath.split("#").mapIndexed {
            index, string ->
            if (index % 2 != 0) nodeNames.add(string)
        }
        return nodeNames.joinToString("#")
    }




}
package com.workduck.utils


import com.serverless.utils.Constants
import com.serverless.utils.commonPrefixList
import com.serverless.utils.splitIgnoreEmpty
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager

object NodeHelper {

    private val LOG = LogManager.getLogger(NodeHelper::class.java)

    /* returns path of the format nodeName#nodeID#nodeName#nodeID ... */
    fun getLongestExistingPath(nodeHierarchyInformation: List<String>?, nodeNamePath: String): String {

        var longestExistingPath = ""
        nodeHierarchyInformation?.let {
            for (existingNodePath in nodeHierarchyInformation) {
                val longestCommonNamePath = getCommonPrefixNodePath(nodeNamePath, getNamePath(existingNodePath))

                LOG.info(longestCommonNamePath)
                if(longestCommonNamePath != ""){
                    val nodeNamesAndIDs = existingNodePath.split(Constants.DELIMITER)
                    val commonNodeNamesAndIDs = nodeNamesAndIDs.subList(0, 2*longestCommonNamePath.splitIgnoreEmpty(Constants.DELIMITER).size)

                    if(commonNodeNamesAndIDs.size > longestExistingPath.split(Constants.DELIMITER).size)
                        longestExistingPath = commonNodeNamesAndIDs.joinToString(Constants.DELIMITER)
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
        return path1.split(Constants.DELIMITER).commonPrefixList(path2.split(Constants.DELIMITER)).joinToString(Constants.DELIMITER)
    }

    /* nodePath is of the format : node1Name#node1ID#node2Name#node2ID.. */
    fun getNamePath(nodePath: String): String {
        val nodeNames = mutableListOf<String>()
        nodePath.split(Constants.DELIMITER).mapIndexed {
            index, string ->
            if (index % 2 == 0) nodeNames.add(string)
        }
        return nodeNames.joinToString(Constants.DELIMITER)
    }

    fun getIDPath(nodePath: String): String {
        val nodeNames = mutableListOf<String>()
        nodePath.split(Constants.DELIMITER).mapIndexed {
            index, string ->
            if (index % 2 != 0) nodeNames.add(string)
        }
        return nodeNames.joinToString(Constants.DELIMITER)
    }

    fun isExistingPathDividedInRefactor(unchangedNodes: List<String>, existingNodes: List<String>): Boolean{
        return unchangedNodes + existingNodes.last() != existingNodes
    }

    fun removeRedundantPaths(
            updatedPaths: List<String>,
            nodeHierarchy: MutableList<String>
    ): List<String> {

        val pathsToAdd = mutableListOf<String>()
        for (newPath in updatedPaths) {
            var redundantPath = false
            for (existingNodePath in nodeHierarchy) {
                if (newPath.commonPrefixWith(existingNodePath) == newPath) {
                    redundantPath = true
                    break
                }
            }
            if(!redundantPath) pathsToAdd.add(newPath)
        }
        nodeHierarchy += pathsToAdd

        return nodeHierarchy.distinct()
    }





}
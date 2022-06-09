package com.workduck.utils


import com.serverless.models.requests.NodePath
import com.serverless.utils.Constants
import com.serverless.utils.commonPrefixList
import com.serverless.utils.splitIgnoreEmpty
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager
import org.w3c.dom.Node

object NodeHelper {

    private val LOG = LogManager.getLogger(NodeHelper::class.java)

    /* returns path of the format nodeName#nodeID#nodeName#nodeID ... */
    fun getLongestExistingPath(nodeHierarchyInformation: List<String>?, nodeNamePath: String): String {

        var longestExistingPath = ""
        nodeHierarchyInformation?.let {
            for (existingNodePath in nodeHierarchyInformation) {
                val longestCommonNamePath = getCommonPrefixNodePath(nodeNamePath, getNamePath(existingNodePath))

                LOG.debug(longestCommonNamePath)
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
        LOG.debug(longestExistingPath)
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

    fun isNodeIDInPath(nodePath: String, nodeID: String) : Boolean {
        return nodePath.split(Constants.DELIMITER).contains(nodeID)
    }

    fun isRename(existingNodes: NodePath, newNodes: NodePath) : Boolean{
        return existingNodes.allNodes.last() != newNodes.allNodes.last()
    }


}
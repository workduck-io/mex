package com.workduck.utils

import com.serverless.models.requests.NodeNamePath
import com.serverless.models.requests.NodePath
import com.serverless.utils.Constants
import com.serverless.utils.addAlphanumericStringToTitle
import com.serverless.utils.commonPrefixList
import com.serverless.utils.convertToPathString
import com.serverless.utils.getListOfNodes
import com.serverless.utils.splitIgnoreEmpty
import com.workduck.models.Node
import org.apache.logging.log4j.LogManager

object NodeHelper {

    private val LOG = LogManager.getLogger(NodeHelper::class.java)

    /* returns path of the format nodeName#nodeID#nodeName#nodeID ... */
    fun getLongestExistingPathFromNamePath(nodeHierarchyInformation: List<String>?, nodeNamePath: String): String {

        var longestExistingPath = ""
        nodeHierarchyInformation?.let {
            for (existingNodePath in nodeHierarchyInformation) {
                val longestCommonNamePath = getCommonPrefixNodePath(nodeNamePath, getNamePath(existingNodePath))

                LOG.debug(longestCommonNamePath)
                if (longestCommonNamePath != "") {
                    val nodeNamesAndIDs = existingNodePath.split(Constants.DELIMITER)
                    val commonNodeNamesAndIDs = nodeNamesAndIDs.subList(0, 2 * longestCommonNamePath.splitIgnoreEmpty(Constants.DELIMITER).size)

                    if (commonNodeNamesAndIDs.size > longestExistingPath.split(Constants.DELIMITER).size)
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

    fun updateNodePath(longestExistingPathBasedOnNames: String, nodePath: NodePath, node: Node): String {
        require(longestExistingPathBasedOnNames != nodePath.path
                && longestExistingPathBasedOnNames.commonPrefixWith(nodePath.path) != nodePath.path) { "Same hierarchy with same nodeIDs exists" } /* user is in sync with backend */

        if(longestExistingPathBasedOnNames.isEmpty()) return longestExistingPathBasedOnNames


        val nodeNamesFromLongestExistingPath = getNamePath(longestExistingPathBasedOnNames).getListOfNodes()
        val nodeIDsFromLongestExistingPath = getIDPath(longestExistingPathBasedOnNames).getListOfNodes()

        var indexTillActualCommonNode = -1
        var isNodeIDMismatched = false
        for ((index, existingNodeName) in getNamePath(nodePath.path).getListOfNodes().withIndex()) {
            if (index + 1 > nodeNamesFromLongestExistingPath.size) break /* when no. of nodes in passed path size exceeds nodes from the longest path */

            if (existingNodeName == nodeNamesFromLongestExistingPath[index]) {
                if (nodePath.allNodesIDs[index] == nodeIDsFromLongestExistingPath[index]) { /* node name & node id is same */
                    if(isNodeIDMismatched) throw IllegalArgumentException("Path Invalid")
                    indexTillActualCommonNode = 2*index + 1
                    continue
                }
                else { /* node name is same but id is different => change the name */
                    isNodeIDMismatched = true
                    nodePath.allNodesNames[index] = existingNodeName.addAlphanumericStringToTitle()
                }
            }
        }
        node.title = nodePath.allNodesNames.last()
        updateNodePathFromNamesAndIDs(nodePath)

        return if(indexTillActualCommonNode == -1) ""
        else longestExistingPathBasedOnNames.getListOfNodes().take(indexTillActualCommonNode - 1).convertToPathString()
    }


    private fun updateNodePathFromNamesAndIDs(nodePath: NodePath){
        nodePath.path = nodePath.allNodesNames.take(nodePath.allNodesNames.size)
                        .zip(nodePath.allNodesIDs.take(nodePath.allNodesNames.size)) { name, id -> "$name${Constants.DELIMITER}$id" }
                        .joinToString(Constants.DELIMITER)
    }

//    fun getLongestExistingPathFromNodePath(nodeHierarchyInformation: List<String>?, nodePath: String): String {
//        val longestExistingPathFromNames = getLongestExistingPathFromNamePath(nodeHierarchyInformation, getNamePath(nodePath))
//
//        var longestExistingPath = "" /* this path contains both nodeNames and nodeIDs */
//        nodeHierarchyInformation?.let {
//            for (existingNodePath in nodeHierarchyInformation) {
//                val longestCommonPath = getCommonPrefixNodePath(nodePath, existingNodePath)
//                if(longestCommonPath.getListOfNodes().size > longestExistingPath.getListOfNodes().size){
//                    longestExistingPath = longestCommonPath
//                }
//            }
//        }
//
//        /* now change nodePath */
//
//        val namePathFromLongestExistingPath = getNamePath(longestExistingPath)
//        if(namePathFromLongestExistingPath == getNamePath(longestExistingPathFromNames)){ /* user is in sync with backend */
//            return longestExistingPath
//        }
//        else{ /* some nodes in hierarchy are already present but not present with the user */
//
//            val listOfUserNodeIDs = getIDPath(longestExistingPath).getListOfNodes()
//            for( (existingNodeID, index) in getIDPath(longestExistingPathFromNames).getListOfNodes().withIndex()){
//                if(existingNodeID != listOfUserNodeIDs[index])
//
//            }
//        }
//
//    }
//
//
//
//
//
//
//
//
//

    fun getCommonPrefixNodePath(path1: String, path2: String): String {
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

    fun isNodeIDInPath(nodePath: String, nodeID: String): Boolean {
        return nodePath.split(Constants.DELIMITER).contains(nodeID)
    }

    fun isRename(existingNodes: NodeNamePath, newNodes: NodeNamePath): Boolean {
        return existingNodes.allNodes.last() != newNodes.allNodes.last()
    }

    fun isPathClashing(editedNodeTitle: String, passedNodeTitle: String): Boolean {
        return editedNodeTitle != passedNodeTitle
    }

    fun checkForDuplicateNodeID(nodeHierarchyInformation: List<String>, nodeID: String) {
        if (nodeHierarchyInformation.any { it.contains(nodeID) }) throw IllegalArgumentException("NodeID already exists")
    }
}

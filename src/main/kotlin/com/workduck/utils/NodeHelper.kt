package com.workduck.utils

import com.serverless.models.requests.NodeNamePath
import com.serverless.models.requests.NodePath
import com.serverless.utils.Constants
import com.serverless.utils.extensions.addAlphanumericStringToTitle
import com.serverless.utils.extensions.addIfNotEmpty
import com.serverless.utils.extensions.commonPrefixList
import com.serverless.utils.extensions.convertToPathString
import com.serverless.utils.extensions.getListFromPath
import com.serverless.utils.extensions.splitIgnoreEmpty
import com.workduck.models.MatchType
import com.workduck.models.Namespace
import com.workduck.models.Node
import com.workduck.models.NodeOperationType
import org.apache.logging.log4j.LogManager

object NodeHelper {

    private val LOG = LogManager.getLogger(NodeHelper::class.java)

    /* matches the longest path from hierarchy with nodeNamePath and returns path of the format nodeName#nodeID#nodeName#nodeID ... */
    fun getLongestExistingPathFromNamePath(nodeNamePath: String, hierarchy: List<String>): String {

        return getLongestExistingPath(hierarchy, nodeNamePath)


    }

    fun getLongestExistingPath(nodeHierarchyInformation: List<String>?, nodeNamePath: String) : String{
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


        val nodeNamesFromLongestExistingPath = getNamePath(longestExistingPathBasedOnNames).getListFromPath()
        val nodeIDsFromLongestExistingPath = getIDPath(longestExistingPathBasedOnNames).getListFromPath()

        var indexTillActualCommonNode = -1
        var isNodeIDMismatched = false
        for ((index, existingNodeName) in getNamePath(nodePath.path).getListFromPath().withIndex()) { /* iterate through nodePath nodes */
            if (index + 1 > nodeNamesFromLongestExistingPath.size) break /* when no. of nodes in passed path size exceeds nodes from the longest path */

            if (existingNodeName == nodeNamesFromLongestExistingPath[index]) {
                if (nodePath.allNodesIDs[index] == nodeIDsFromLongestExistingPath[index]) { /* node name & node id is same */
                    if(isNodeIDMismatched) throw IllegalArgumentException("Path Invalid") /* valid once isNodeIDMismatched is set to true */
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
        else longestExistingPathBasedOnNames.getListFromPath().take(indexTillActualCommonNode + 1).convertToPathString()
    }




    fun updateHierarchiesInNamespace(namespace: Namespace, passedNodeIDList: List<String>, operationType : NodeOperationType) {
        val sourceHierarchy = when(operationType){
            NodeOperationType.ARCHIVE -> namespace.nodeHierarchyInformation /* move from active to archived hierarchy */
            NodeOperationType.UNARCHIVE -> namespace.archivedNodeHierarchyInformation /* move from archived to active hierarchy */
            NodeOperationType.DELETE -> namespace.archivedNodeHierarchyInformation /* delete from archived hierarchy */
        }

        val newSourceHierarchy = mutableListOf<String>()

        val newDestinationHierarchy = when(operationType){
            NodeOperationType.ARCHIVE -> namespace.archivedNodeHierarchyInformation.toMutableList() /* move from active to archived hierarchy */
            NodeOperationType.UNARCHIVE -> namespace.nodeHierarchyInformation.toMutableList() /* move from archived to active hierarchy */
            NodeOperationType.DELETE -> null /* delete from archived hierarchy */
        }

        updateHierarchiesInArchiveUnarchive(sourceHierarchy, newSourceHierarchy, newDestinationHierarchy, passedNodeIDList)

        when(operationType){
            NodeOperationType.ARCHIVE -> Namespace.populateHierarchiesAndUpdatedAt(namespace, activeHierarchy = newSourceHierarchy, archivedHierarchy = newDestinationHierarchy)
            NodeOperationType.UNARCHIVE -> Namespace.populateHierarchiesAndUpdatedAt(namespace, activeHierarchy = newDestinationHierarchy , archivedHierarchy = newSourceHierarchy)
            NodeOperationType.DELETE -> Namespace.populateHierarchiesAndUpdatedAt(namespace, activeHierarchy = newDestinationHierarchy, archivedHierarchy = newSourceHierarchy)
        }

    }


    /* sourceHierarchy : Hierarchy to move nodes from.
       In case of archiving, sourceHierarchy will be active hierarchy
       In case of unarchiving, sourceHierarchy will be archived hierarchy

       newSourceHierarchy : Updated Hierarchy from which nodes were moved.
       In case of archiving, newSourceHierarchy will be newActiveHierarchy hierarchy
       In case of unarchiving, newSourceHierarchy will be newArchivedHierarchy hierarchy


       newDestinationHierarchy : Updated Hierarchy to which nodes were moved.
       In case of archiving, newDestinationHierarchy will be newArchivedHierarchy hierarchy
       In case of unarchiving, newDestinationHierarchy will be newActiveHierarchy hierarchy

       Can also use this to delete nodes from archivedHierarchy.
       In that case :
       sourceHierarchy will be archived hierarchy
       newSourceHierarchy will be newArchivedHierarchy hierarchy
       newDestinationHierarchy will be null

     */
    private fun updateHierarchiesInArchiveUnarchive(
        sourceHierarchy: List<String>,
        newSourceHierarchy: MutableList<String>,
        newDestinationHierarchy: MutableList<String>?,
        passedNodeIDList: List<String>
    ) {

        for (nodePath in sourceHierarchy) {
            var isNodePresentInPath = false
            val pathsListForSinglePath = mutableListOf<String>() /* more than one node ids from a single path could be passed */
            for (nodeID in passedNodeIDList) {
                if (nodePath.contains(nodeID)) {
                    isNodePresentInPath = true
                    pathsListForSinglePath.add(
                        nodePath.getListFromPath().let {
                            it.subList(it.indexOf(nodeID) - 1, it.size)
                        }.convertToPathString()
                    )
                }
            }
            if (isNodePresentInPath) {
                val finalPathToArchive = WorkspaceHelper.removeRedundantPaths(pathsListForSinglePath, MatchType.SUFFIX)[0]
                newDestinationHierarchy?.add(finalPathToArchive)
                /* active hierarchy is nodePath minus the archived path */
                newSourceHierarchy.addIfNotEmpty(nodePath.getListFromPath().dropLast(finalPathToArchive.getListFromPath().size).convertToPathString())
            } else { /* this path will remain unchanged */
                newSourceHierarchy.add(nodePath)
            }
        }

        newDestinationHierarchy?.let { WorkspaceHelper.removeRedundantPaths(it) }
        WorkspaceHelper.removeRedundantPaths(newSourceHierarchy)
    }


    private fun updateNodePathFromNamesAndIDs(nodePath: NodePath){
        nodePath.path = nodePath.allNodesNames.take(nodePath.allNodesNames.size)
                        .zip(nodePath.allNodesIDs.take(nodePath.allNodesNames.size)) { name, id -> "$name${Constants.DELIMITER}$id" }
                        .joinToString(Constants.DELIMITER)
    }

    fun getNodeIDsFromHierarchy(hierarchiesList : List<String>) : List<String> {
        if(hierarchiesList.isEmpty()) return listOf()
        return hierarchiesList.map { nodePath ->
            getIDPath(nodePath).getListFromPath()
        }.flatten().toSet().toList()

    }


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
        require (nodeHierarchyInformation.none { it == nodeID }) { "NodeID already exists" }
    }
}

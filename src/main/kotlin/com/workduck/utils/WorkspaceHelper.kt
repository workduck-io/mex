package com.workduck.utils

import com.serverless.utils.commonPrefixList
import com.serverless.utils.commonSuffixList
import com.serverless.utils.getListOfNodes
import com.workduck.models.MatchType

object WorkspaceHelper {

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
            if (!redundantPath) pathsToAdd.add(newPath)
        }
        nodeHierarchy += pathsToAdd

        return nodeHierarchy.distinct()
    }

    fun removeRedundantPaths(
        nodeHierarchy: MutableList<String>,
        matchType: MatchType = MatchType.PREFIX
    ): List<String> {

        val nonRedundantHierarchy = mutableListOf<String>()
        for (path in nodeHierarchy) {
            var redundantPath = false
            for (pathToCompare in nodeHierarchy) {
                if (path != pathToCompare) {
                    when (matchType) {
                        MatchType.PREFIX -> {
                            if (path.getListOfNodes().commonPrefixList(pathToCompare.getListOfNodes()) == path.getListOfNodes()) {
                                redundantPath = true
                                break
                            }
                        }
                        MatchType.SUFFIX -> {
                            if (path.getListOfNodes().commonSuffixList(pathToCompare.getListOfNodes()) == path.getListOfNodes()) {
                                redundantPath = true
                                break
                            }
                        }
                    }
                }
            }
            if (!redundantPath) nonRedundantHierarchy.add(path)
        }

        return nonRedundantHierarchy.distinct()
    }
}

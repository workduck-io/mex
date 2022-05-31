package com.workduck.utils

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
            if(!redundantPath) pathsToAdd.add(newPath)
        }
        nodeHierarchy += pathsToAdd

        return nodeHierarchy.distinct()
    }

    fun removeRedundantPaths(
            nodeHierarchy: MutableList<String>
    ): List<String> {

        val nonRedundantHierarchy = mutableListOf<String>()
        for (path in nodeHierarchy) {
            var redundantPath = false
            for (pathToCompare in nodeHierarchy) {
                if (path != pathToCompare) {
                    if (path.commonPrefixWith(pathToCompare) == path) {
                        redundantPath = true
                        break
                    }
                }
            }
            if(!redundantPath) nonRedundantHierarchy.add(path)
        }

        return nonRedundantHierarchy.distinct()
    }
}
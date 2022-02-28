package com.serverless.utils

import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

object RelationshipHelper {

    fun handleRelationDeletion(startNode: String?, endNode: String?, workspaceID: String?, workspaceService: WorkspaceService){
        workspaceID?.let {
            val workspace = workspaceService.getWorkspace(workspaceID) as Workspace
            val nodeHierarchyInformation = workspace.nodeHierarchyInformation

            val combinedSubString = "$startNode#$endNode"
            nodeHierarchyInformation?.let {
                it.filter { nodePath ->
                    nodePath.contains(combinedSubString)
                }.forEach{ targetString ->

                }
            }


        } ?: throw Exception("Workspace ID Cannot be null")


    }


    fun handleRelationAddition(startNode: String?, endNode: String?, workspaceID: String?, workspaceService: WorkspaceService){
        require(startNode != null && endNode != null && workspaceID != null) { throw Exception("StartNode, EndNode and WorkspaceID cannot be null")}

        val workspace = workspaceService.getWorkspace(workspaceID) as Workspace

        if(startNode == endNode){
            workspaceService.updateNodeHierarchyInformation()
        }

        val nodeHierarchyInformation = workspace.nodeHierarchyInformation

        val combinedSubString = "$startNode#$endNode"

        val newNodeHierarchyInformation = mutableListOf<String>()

        var appendAtEnd = false

        for(nodePath in nodeHierarchyInformation!!){
            if(nodePath.endsWith(startNode)){
                appendAtEnd = true
                break
            }



        }


    }



}
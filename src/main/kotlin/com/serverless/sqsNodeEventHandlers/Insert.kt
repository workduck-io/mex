package com.serverless.sqsNodeEventHandlers

import com.workduck.models.Node
import com.workduck.service.NodeService
import com.workduck.utils.Helper


class Insert : Action {
    override fun apply(ddbPayload: DDBPayload, nodeService: NodeService) {



        val newNodeImage = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(ddbPayload.NewImage))
        val node : Node = Node.convertNodeImageToNode(newNodeImage as NodeImage)

        nodeService.createNodeVersion(node)

        //println(Helper.objectMapper.writeValueAsString(newNode))
    }
}
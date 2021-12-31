package com.serverless.sqsNodeEventHandlers

import com.workduck.models.Node
import com.workduck.service.NodeService
import com.workduck.utils.Helper

class Modify : Action {
    override fun apply(ddbPayload: DDBPayload, nodeService: NodeService) {

        val oldNodeImage = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(ddbPayload.OldImage)) as NodeImage
        val newNodeImage = EventHelper.getImageObjectFromImage(Helper.objectMapper.writeValueAsString(ddbPayload.NewImage)) as NodeImage

        val newNode : Node = Node.convertNodeImageToNode(newNodeImage as NodeImage)
        nodeService.createNodeVersion(newNode)
    }
}
package com.serverless.sqsTriggers.nodeDeleteSQSWorkers

import com.workduck.models.Node
import com.workduck.service.NodeService
import com.workduck.utils.TagHelper

class DeleteTagsAndNodeStrategy : NodeDelete {
    override fun deleteNode(node: Node, nodeService: NodeService) {

        /* node is already marked as deleted so just delete the tags associated with the node */
        val tags = NodeDeleteWorker.nodeService.nodeRepository.getTags(node.id, node.workspaceIdentifier.id)
        if (!tags.isNullOrEmpty()) {
            TagHelper.deleteTags(tags, node.id, node.workspaceIdentifier.id)
        }

    }

}

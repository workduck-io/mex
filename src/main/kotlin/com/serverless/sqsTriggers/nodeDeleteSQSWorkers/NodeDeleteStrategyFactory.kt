package com.serverless.sqsTriggers.nodeDeleteSQSWorkers

import com.serverless.sqsTriggers.namespaceDeleteSQSWorkers.DeleteNamespaceWithSuccessorStrategy
import com.serverless.sqsTriggers.namespaceDeleteSQSWorkers.DeleteNamespaceWithoutSuccessorStrategy
import com.workduck.models.Namespace
import com.workduck.models.Node

class NodeDeleteStrategyFactory {
    companion object {
        fun getNodeDeleteStrategy(node : Node): NodeDelete {
            return DeleteTagsAndNodeStrategy()
        }
    }
}
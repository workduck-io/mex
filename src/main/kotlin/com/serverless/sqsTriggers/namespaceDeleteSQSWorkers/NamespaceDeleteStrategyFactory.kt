package com.serverless.sqsTriggers.namespaceDeleteSQSWorkers

import com.workduck.models.Namespace

class NamespaceDeleteStrategyFactory {
    companion object {
        fun getNamespaceDeleteStrategy(namespace : Namespace): NamespaceDelete {
            val successorWorkspaceID  = namespace.successorNamespace?.id
            return if(!successorWorkspaceID.isNullOrEmpty()){
                DeleteNamespaceWithSuccessorStrategy()
            } else{
                DeleteNamespaceWithoutSuccessorStrategy()
            }
        }
    }
}
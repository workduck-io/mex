package com.serverless.utils

import com.serverless.models.Response
import com.serverless.transformers.NamespaceTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Namespace

object NamespaceHelper {

    val namespaceTransformer : Transformer<Namespace> = NamespaceTransformer()


    fun convertNamespaceToNamespaceResponse(namespace: Entity?) : Response? {
        return namespaceTransformer.transform(namespace as Namespace?)
    }
}
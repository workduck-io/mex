package com.serverless.utils

import com.serverless.models.requests.NodePath
import com.serverless.models.requests.NodeRequest
import com.workduck.models.Node
import com.workduck.models.WorkspaceIdentifier

fun NodeRequest.toNode(workspaceID: String): Node =
    Node(
        id = this.id,
        title = this.title,
        namespaceIdentifier = this.namespaceIdentifier,
        workspaceIdentifier = WorkspaceIdentifier(workspaceID),
        lastEditedBy = this.lastEditedBy,
        tags = this.tags,
        data = this.data
    )


fun NodePath.removePrefix(prefix: String): String {
    return this.path.removePrefix(prefix)
}

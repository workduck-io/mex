package com.workduck.utils.extensions

import com.serverless.models.requests.NodeBulkRequest
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.WorkspaceRequest
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.Node
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier

fun NodeRequest.toNode(workspaceID: String, userID: String): Node =
        Node(
                id = this.id,
                title = this.title,
                namespaceIdentifier = this.namespaceIdentifier,
                workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                lastEditedBy = userID,
                tags = this.tags ,
                data = this.data
        )


fun NodeBulkRequest.toNode(workspaceID: String, userID: String): Node {
    val node = Node(
            id = this.id,
            title = this.title,
            workspaceIdentifier = WorkspaceIdentifier(workspaceID),
            lastEditedBy = userID,
            tags = this.tags,
            data = this.data
    )
    node.namespaceIdentifier = this.nodePath.namespaceID?.let { NamespaceIdentifier(it) }
    return node
}

fun WorkspaceRequest.toWorkspace(workspaceID: String) : Workspace {
    return Workspace(
            id = workspaceID,
            name = this.name
    )
}

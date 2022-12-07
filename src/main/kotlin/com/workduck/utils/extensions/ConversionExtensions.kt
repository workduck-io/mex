package com.workduck.utils.extensions

import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.NamespaceRequest
import com.serverless.models.requests.NodeBulkRequest
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.SnippetRequest
import com.serverless.models.requests.UpdateSharedNodeRequest
import com.serverless.models.requests.WorkspaceRequest
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
import com.workduck.models.Namespace
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.Node
import com.workduck.models.Page
import com.workduck.models.Snippet
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.utils.PageHelper

fun NodeRequest.toNode(workspaceID: String, userID: String): Node =
        Node(
                id = this.id,
                title = this.title,
                namespaceIdentifier = this.namespaceIdentifier,
                workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                lastEditedBy = userID,
                tags = this.tags ,
                data = this.data,
                metadata = this.pageMetadata
        )

fun UpdateSharedNodeRequest.toNode(workspaceID: String, namespaceID: String, userID: String): Node =
        Node(
                id = this.id,
                title = this.title,
                namespaceIdentifier = NamespaceIdentifier(namespaceID),
                workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                lastEditedBy = userID,
                tags = this.tags ,
                data = this.data,
                metadata = this.pageMetadata
        )



fun NodeBulkRequest.toNode(nodeID: String, nodeTitle: String, workspaceID: String, userID: String): Node {
    val node = Node(
            id = nodeID,
            title = nodeTitle,
            workspaceIdentifier = WorkspaceIdentifier(workspaceID),
            lastEditedBy = userID,
            tags = this.tags,
            data = this.data,
            metadata = this.pageMetadata
    )
    node.namespaceIdentifier = this.nodePath.namespaceID.let { NamespaceIdentifier(it) }
    return node
}


fun SnippetRequest.createSnippetObjectFromSnippetRequest(userID: String, workspaceID: String): Snippet =
    Snippet(
        id = this.id,
        workspaceIdentifier = WorkspaceIdentifier(workspaceID),
        lastEditedBy = userID,
        data = this.data,
        title = this.title,
        version = this.version,
        template = this.template,
        metadata = this.pageMetadata
    )



fun WorkspaceRequest.toWorkspace(workspaceID: String) : Workspace {
    return Workspace(
            id = workspaceID,
            name = this.name
    )
}

fun NamespaceRequest.toNamespace(workspaceID: String, ownerUserID: String?) : Namespace {
    return Namespace(
            id = this.id,
            name = this.name,
            createdBy = ownerUserID,
            namespaceMetadata = this.namespaceMetadata,
            workspaceIdentifier = WorkspaceIdentifier(workspaceID))

}

fun GenericListRequest.toIDList() : List<String> {
    return this.ids
}


fun GenericListRequest.toNodeIDList() : List<String> {
    require( this.ids.none { nodeID -> !nodeID.isValidID(Constants.NODE_ID_PREFIX) } ) { "Invalid NodeID(s)" }
    return this.ids
}

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

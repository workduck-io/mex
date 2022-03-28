import com.serverless.models.requests.NodeRequest
import com.workduck.models.Node
import com.workduck.models.WorkspaceIdentifier

fun NodeRequest?.toNode(workspaceID : String) : Node? =
    this?.let {
        Node(
            id = this.id,
            title = this.title,
            namespaceIdentifier = this.namespaceIdentifier,
            workspaceIdentifier = WorkspaceIdentifier(workspaceID),
            lastEditedBy = this.lastEditedBy,
            tags = this.tags,
            data = this.data
        )
    }
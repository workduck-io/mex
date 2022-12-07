package com.serverless.utils

object Messages {


    const val ERROR_CREATING_NODE = "Error creating node"
    const val ERROR_GETTING_NODE = "Error getting node"
    const val ERROR_GETTING_NODE_VERSIONS = "Error getting node versions"
    const val ERROR_GETTING_NODES = "Error getting nodes"
    const val ERROR_NODE_PERMISSION = "Either the node does not exist or you do not have permissions"
    const val INVALID_NODE_ID  = "Invalid NodeID"
    const val INVALID_BLOCK_ID  = "Invalid BlockID"
    const val INVALID_NODE_STATE  = "Invalid Node state"

    const val ERROR_MOVING_BLOCK = "Error moving node"

    const val NODE_NOT_AVAILABLE = "Node not available"

    const val ERROR_APPENDING_NODE = "Error appending to node"
    const val ERROR_UPDATING_NODE_BLOCK = "Error updating node block"
    const val ERROR_UPDATING_NODE_METADATA = "Error updating node metadata"


    const val ERROR_DELETING_NODE = "Error deleting node"
    const val ERROR_UNARCHIVING_NODE = "Error un-archiving node"
    const val ERROR_ARCHIVING_NODE = "Error archiving node"
    const val ERROR_UPDATING_NODE = "Error updating node"
    const val ERROR_UPDATING_ARCHIVED_NODE = "Cannot update archived node"

    const val ERROR_MAKING_NODE_PUBLIC = "Error making node public"
    const val ERROR_MAKING_NODE_PRIVATE = "Error making node private"

    const val ERROR_DELETING_NODES_NOT_IN_NAMESPACE = "The passed NodeIDs should be present in the Namespace and archived"

    const val ERROR_UPDATING_NODE_PATH = "Error updating node path"


    const val ERROR_SHARING_NODE = "Error sharing node"
    const val ERROR_UPDATING_ACCESS = "Error updating access"

    const val ERROR_REGISTERING_USER = "Error registering user"
    const val ERROR_UPDATING_USER = "Error updating user"


    const val ERROR_GETTING_WORKSPACE = "Error getting workspace"
    const val ERROR_GETTING_WORKSPACES = "Error getting workspaces"
    const val ERROR_UPDATING_WORKSPACE = "Error updating workspace"
    const val ERROR_CREATING_WORKSPACE = "Error creating workspace"
    const val ERROR_GETTING_HIERARCHY = "Error getting node hierarchy for workspace"
    const val ERROR_REFRESHING_HIERARCHY = "Error refreshing node hierarchy for workspace"
    const val ERROR_DELETING_WORKSPACE = "Error deleting workspace"

    const val ERROR_SHARING_NAMESPACE = "Error sharing namespace"
    const val ERROR_MAKING_NAMESPACE_PUBLIC = "Error making namespace public"
    const val ERROR_GETTING_NAMESPACE = "Error getting namespace"
    const val ERROR_GETTING_NAMESPACES = "Error getting namespaces"
    const val ERROR_UPDATING_NAMESPACE = "Error updating namespace"
    const val ERROR_CREATING_NAMESPACE = "Error creating namespace"
    const val ERROR_DELETING_NAMESPACE = "Error deleting namespace"
    const val ERROR_NAMESPACE_PERMISSION = "Either the namespace does not exist or you do not have permissions"
    const val ERROR_NAMESPACE_PRIVATE = "Namespace already private"
    const val ERROR_NAMESPACE_PUBLIC = "Namespace already public"
    const val INVALID_NAMESPACE_ID  = "Invalid NamespaceID"



    const val ERROR_CREATING_SNIPPET = "Error creating snippet"
    const val ERROR_GETTING_SNIPPET = "Error getting snippet"
    const val ERROR_GETTING_SNIPPETS = "Error getting snippets"
    const val ERROR_DELETING_SNIPPET = "Error deleting snippet"
    const val ERROR_DELETING_SNIPPETS = "Error deleting snippets"
    const val ERROR_SNIPPET_PUBLIC = "Error making snippet public"
    const val ERROR_SNIPPET_PRIVATE = "Error making snippet private"
    const val INVALID_SNIPPET_ID = "Invalid SnippetID"
    const val ERROR_CLONING_SNIPPET = "Error cloning snippet"
    const val ERROR_UPDATING_SNIPPET_METADATA = "Error updating snippet metadata"


    const val ERROR_GETTING_TAGS = "Error fetching tags"

    const val INVALID_WORKSPACE_ID = "Invalid Workspace ID"

    const val ERROR_GETTING_RECORDS = "Error getting records"
    const val ERROR_UPDATING_RECORDS ="Error updating preferences"


    const val ERROR_GETTING_STARRED = "Error getting starred nodes of the user"
    const val ERROR_DELETING_STARRED = "Error removing star(s)"
    const val ERROR_CREATING_STARRED = "Error creating star(s)"




    const val MALFORMED_REQUEST = "Malformed Request"
    const val REQUEST_NOT_RECOGNIZED = "Request not recognized"
    const val INVALID_PARAMETERS = "Invalid Parameters"
    const val INVALID_ID = "Invalid ID"

    const val NODE_IDS_DO_NOT_EXIST = "NodeIDs don't exist"
    const val SOURCE_ID_DESTINATION_ID_SAME = "Source NodeID can't be equal to Destination NodeID"


    const val UNAUTHORIZED = "Unauthorized"
    const val INTERNAL_SERVER_ERROR = "Internal Server Error"
    const val ITEM_SIZE_LARGE = "Item size has exceeded the maximum allowed size"
    const val RESOURCE_NOT_FOUND = "Requested resource not found"
    const val BAD_REQUEST = "Bad Request"
    const val NPE = "Getting NPE"

    const val ERROR_DELETING_BLOCK = "Error deleting block"
}
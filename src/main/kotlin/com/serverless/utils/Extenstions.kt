package com.serverless.utils

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.requests.NodeBulkRequest
import com.serverless.models.requests.NodePath
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.SnippetRequest
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.Node
import com.workduck.models.Snippet
import com.workduck.models.WorkspaceIdentifier
import com.workduck.utils.PageHelper
import org.apache.logging.log4j.Logger

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

fun Node.isNodeAndTagsUnchanged(storedNode: Node) : Boolean {
    /* also updated block level metadata */
    return !PageHelper.comparePageWithStoredPage(this, storedNode) && this.tags.sorted() == storedNode.tags.sorted()
}


fun SnippetRequest.createSnippetObjectFromSnippetRequest(userID: String, workspaceID: String): Snippet =
    Snippet(
            id = this.id,
            workspaceIdentifier = WorkspaceIdentifier(workspaceID),
            lastEditedBy = userID,
            data = this.data,
            title = this.title,
            version = this.version
    )


fun Snippet.setVersion(version: Int){
    this.version = version
    this.sk = "${this.id}${Constants.DELIMITER}$version"
}

fun NodePath.removePrefix(prefix: String): String {
    return this.path.removePrefix(prefix)
}


fun CharSequence.splitIgnoreEmpty(vararg delimiters: String): List<String> {
    return this.split(*delimiters).filter {
        it.isNotEmpty()
    }
}

fun List<String>.commonPrefixList(list: List<String>): List<String> {
    val commonPrefixList = mutableListOf<String>()
    for (index in 0 until minOf(this.size, list.size)) {
        if (this[index] == list[index]) commonPrefixList.add(this[index])
        else break
    }
    return commonPrefixList
}

fun List<String>.commonSuffixList(list: List<String>): List<String> {
    return this.reversed().commonPrefixList(list.reversed()).reversed()
}

fun List<String>.convertToPathString(delimiter: String = Constants.DELIMITER): String {
    return this.joinToString(delimiter)
}

fun List<String>.getNodesAfterIndex(index: Int): List<String> {
    require(index >= 0 && index < this.size) {
        "Index out of bound"
    }
    return if (index + 1 == this.size) listOf()
    else {
        this.subList(index + 1, this.size)
    }
}

fun List<String>.isNewPathCreated(removedPath: List<String>) : Boolean {
    return this.size == 1 && removedPath.isEmpty()
}

fun List<String>.isRefactorWithNodeAddition(removedPath: List<String>) : Boolean {
    return this.size > 1 && removedPath.size > 1 && this.size == removedPath.size
}

fun List<String>.isRefactorWithPathDivision(removedPath: List<String>) : Boolean {
    return this.size > 1 && removedPath.isNotEmpty() && this.size == removedPath.size + 1
}


fun List<String>.getDifferenceWithOldHierarchy(oldHierarchy : List<String>) : Map<String, List<String>>{
    return mapOf(Constants.REMOVED_PATHS to oldHierarchy.minus(this.toSet())
                ,Constants.ADDED_PATHS to this.minus(oldHierarchy.toSet()) )
}

fun List<String>.isSingleNodePassed(existingNodes: List<String>) : Boolean{
    return this.size == 1 && existingNodes.size == 1
}

fun String.getNewPath(suffix: String) : String {
    return if(this.isEmpty()) suffix
    else "$this${Constants.DELIMITER}$suffix"
}

fun <T> List<T>.mix(other: List<T>): List<T> {
    val first = iterator()
    val second = other.iterator()
    val list = ArrayList<T>(minOf(this.size, other.size))
    while (first.hasNext() && second.hasNext()) {
        list.add(first.next())
        list.add(second.next())
    }
    return list
}


fun String.isLastNodeSame(path: String, delimiter: String = Constants.DELIMITER) : Boolean {
    return this.split(delimiter).last() == path.split(delimiter).last()
}


fun String.getListOfNodes(delimiter: String = Constants.DELIMITER): List<String> {
    return this.split(delimiter)
}

fun String.containsExistingNodes(existingNodes: List<String>, delimiter: String = Constants.DELIMITER) : Boolean {
    return this.getListOfNodes(delimiter).commonPrefixList(existingNodes) == existingNodes
}



fun Map<String, Any>.handleWarmup(LOG : Logger) : ApiGatewayResponse? {
    return if(this["source"] as String? == "serverless-plugin-warmup"){
        LOG.info("WarmUp - Lambda is warm!")
        ApiResponseHelper.generateStandardResponse("Warming Up",  "")
    } else null
}

fun String.createNodePath(nodeID : String) : String{
    return "$this${Constants.DELIMITER}$nodeID"
}
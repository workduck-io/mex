package com.serverless.utils

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.requests.NodePath
import com.serverless.models.requests.SnippetRequest
import com.workduck.models.Entity
import com.workduck.models.Node
import com.workduck.models.Snippet
import com.workduck.models.WorkspaceIdentifier
import com.workduck.utils.Helper
import com.workduck.utils.PageHelper
import kotlinx.coroutines.Deferred
import org.apache.logging.log4j.Logger
import com.workduck.utils.Helper.objectMapper

fun Node.isNodeUnchanged(storedNode: Node): Boolean {
    /* also updated block level metadata */
    return !PageHelper.comparePageDataWithStoredPage(this, storedNode)
            && this.tags.sorted() == storedNode.tags.sorted() && this.nodeMetaData == storedNode.nodeMetaData
}

fun SnippetRequest.createSnippetObjectFromSnippetRequest(userID: String, workspaceID: String): Snippet =
    Snippet(
        id = this.id,
        workspaceIdentifier = WorkspaceIdentifier(workspaceID),
        lastEditedBy = userID,
        data = this.data,
        title = this.title,
        version = this.version,
        template = this.template
    )

fun Snippet.setVersion(version: Int) {
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

fun List<String>.removePrefixList(listToRemove: List<String>): List<String> {
    if(listToRemove.isEmpty()) return this
    var mismatchIndex = -1
    for (index in listToRemove.indices){
        if (this[index] != listToRemove[index])  {
            mismatchIndex = index
            break
        }
    }
    if (mismatchIndex == -1 ) mismatchIndex = listToRemove.size
    return this.subList(mismatchIndex, this.size)
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

fun List<String>.isNewPathCreated(removedPath: List<String>): Boolean {
    return this.size == 1 && removedPath.isEmpty()
}

fun List<String>.isRefactorWithNodeAddition(removedPath: List<String>): Boolean {
    return this.size > 1 && removedPath.size > 1 && this.size == removedPath.size
}

fun List<String>.isRefactorWithPathDivision(removedPath: List<String>): Boolean {
    return this.size > 1 && removedPath.isNotEmpty() && this.size == removedPath.size + 1
}

fun List<String>.getDifferenceWithOldHierarchy(oldHierarchy: List<String>): Map<String, List<String>> {
    return mapOf(Constants.REMOVED_PATHS to oldHierarchy.minus(this.toSet()), Constants.ADDED_PATHS to this.minus(oldHierarchy.toSet()))
}

fun List<String>.isSingleNodePassed(existingNodes: List<String>): Boolean {
    return this.size == 1 && existingNodes.size == 1
}

fun MutableList<String>.addIfNotEmpty(value: String) {
    if (value.isNotEmpty()) this.add(value)
}

fun List<String>.listsEqual(list: List<String>): Boolean{
    return this.size == list.size && this.containsAll(list)
}

fun String.isValidID(prefix: String): Boolean {
    return this.startsWith(prefix) &&
        this.length == prefix.length + Constants.NANO_ID_SIZE &&
        this.takeLast(Constants.NANO_ID_SIZE).isValidNanoID()
}

fun String.isValidTitle() : Boolean {
    return this.filter {
        it.isLetterOrDigit() || Constants.VALID_TITLE_SPECIAL_CHAR.contains(it)
    }.length == this.length
}
fun String.isValidNanoID(): Boolean {
    return this.filter {
        Constants.NANO_ID_RANGE.contains(it)
    }.length == Constants.NANO_ID_SIZE
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

fun String.isLastNodeSame(path: String, delimiter: String = Constants.DELIMITER): Boolean {
    return this.split(delimiter).last() == path.split(delimiter).last()
}

fun String.getListOfNodes(delimiter: String = Constants.DELIMITER): List<String> {
    return this.split(delimiter)
}

fun String.containsExistingNodes(existingNodes: List<String>, delimiter: String = Constants.DELIMITER): Boolean {
    return this.getListOfNodes(delimiter).commonPrefixList(existingNodes) == existingNodes
}

fun String.addAlphanumericStringToTitle() : String {
    return Helper.generateNanoIDCustomLength(this, Constants.TITLE_ALPHANUMERIC_SUFFIX_SIZE)
}

fun Map<String, Any>.handleWarmup(LOG: Logger): ApiGatewayResponse? {
    return if (this["source"] as String? == "serverless-plugin-warmup") {
        LOG.info("WarmUp - Lambda is warm!")
        ApiResponseHelper.generateStandardResponse("Warming Up", "")
    } else null
}

fun String.createNodePath(suffix: String): String {
    if(this.isEmpty()) return suffix
    return "$this${Constants.DELIMITER}$suffix"
}

suspend fun Deferred<Boolean>.awaitAndThrowExceptionIfFalse(booleanJob: Deferred<Boolean>, error: String) {
    if (!(this.await() && booleanJob.await())) throw IllegalArgumentException(error)
}

fun Entity.getRoughSizeOfEntity() : Int{
    return objectMapper.writeValueAsString(this).length
}

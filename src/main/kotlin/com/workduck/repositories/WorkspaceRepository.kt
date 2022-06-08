package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.serverless.utils.Constants
import com.workduck.models.Element
import com.workduck.models.Workspace
import com.workduck.models.Entity
import com.workduck.models.Identifier
import org.apache.logging.log4j.LogManager
import kotlin.math.exp

class WorkspaceRepository(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Workspace> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }


    override fun get(pkIdentifier: Identifier, skIdentifier: Identifier, clazz: Class<Workspace>): Workspace? {
        TODO("Not yet implemented")
    }

    override fun create(t: Workspace): Workspace {
        TODO("Not yet implemented")
    }

    override fun update(t: Workspace): Workspace {
        TODO("Not yet implemented")
    }

    override fun delete(pkIdentifier: Identifier, skIdentifier: Identifier): Identifier {
        TODO("Not yet implemented")
    }

    fun addNodePathToHierarchy(workspaceID: String, nodePath: String){

        LOG.debug("$workspaceID, $nodePath")
        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()
        expressionAttributeValues[":nodePath"] = mutableListOf(nodePath)
        expressionAttributeValues[":empty_list"] = mutableListOf<String>()

        val updateExpression = "set nodeHierarchyInformation = list_append(if_not_exists(nodeHierarchyInformation, :empty_list), :nodePath), updatedAt = :updatedAt"

        try {
            UpdateItemSpec().update(pk = workspaceID, sk = workspaceID, updateExpression = updateExpression,
                    conditionExpression = "attribute_exists(PK) and attribute_exists(SK)", expressionAttributeValues = expressionAttributeValues).let {
                table.updateItem(it)
            }

        }catch (e: ConditionalCheckFailedException){
            LOG.warn("Invalid WorkspaceID : $workspaceID")
        }

    }

    fun getWorkspaceData(workspaceIDList: List<String>): MutableMap<String, Workspace?> {
        val workspaceMap: MutableMap<String, Workspace?> = mutableMapOf()

        for (workspaceID in workspaceIDList) {
            val workspace: Workspace? = mapper.load(Workspace::class.java, workspaceID, workspaceID, dynamoDBMapperConfig)
            workspaceMap[workspaceID] = workspace
        }
        return workspaceMap

    }

    companion object {
        private val LOG = LogManager.getLogger(WorkspaceRepository::class.java)
    }

}

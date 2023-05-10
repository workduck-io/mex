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
import com.workduck.models.Namespace
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager
import kotlin.math.exp

class WorkspaceRepository(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Workspace> {

    private val tableName: String = DDBHelper.getTableName()


    override fun get(pkIdentifier: Identifier, skIdentifier: Identifier, clazz: Class<Workspace>): Workspace? {
        TODO("Not yet implemented")
    }

    override fun create(t: Workspace) {
        TODO("Not yet implemented")
    }

    override fun update(t: Workspace) {
        TODO("Not yet implemented")
    }

    override fun delete(pkIdentifier: Identifier, skIdentifier: Identifier): Identifier {
        TODO("Not yet implemented")
    }

    fun updateWorkspaceName(workspaceID: String, name: String){

        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()
        expressionAttributeValues[":workspaceName"] = name


        val updateExpression = "set workspaceName = :workspaceName, updatedAt = :updatedAt"

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

    fun bulkGetWorkspaces(workspaceIDList : List<String>): List<Workspace> {

        val itemsToGet = workspaceIDList.map { id ->
            Workspace().apply {
                this.id = id
                this.idCopy = id
            }
        }

        val batchResult =  mapper.batchLoad(itemsToGet, dynamoDBMapperConfig)
        val result = batchResult.values.flatten()

        // Cast the result to List<Workspace>
        return result.filterIsInstance<Workspace>()

    }

    fun updateWorkspace(workspace: Workspace) {
        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()

        expressionAttributeValues[":workspaceName"] = workspace.name
        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()


        var updateExpression = ""
        when (workspace.workspaceMetadata != null) {
            true -> {
                expressionAttributeValues[":metadata"] = Helper.objectMapper.writeValueAsString(workspace.workspaceMetadata)
                updateExpression = "SET workspaceName = :workspaceName, updatedAt = :updatedAt, metadata = :metadata"
            }
            false -> {
                updateExpression = "SET workspaceName = :workspaceName, updatedAt = :updatedAt"
            }
        }
        val conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"

        return UpdateItemSpec().update(
            pk = workspace.id, sk = workspace.id, updateExpression = updateExpression,
            expressionAttributeValues = expressionAttributeValues, conditionExpression = conditionExpression
        ).let {
            table.updateItem(it)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(WorkspaceRepository::class.java)
    }

}

package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.workduck.models.Element
import com.workduck.models.Workspace
import com.workduck.models.Entity
import com.workduck.models.Identifier
import org.apache.logging.log4j.LogManager

class WorkspaceRepository(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Workspace> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    override fun get(identifier: Identifier): Entity? {
       return mapper.load(Workspace::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
    }

    override fun delete(identifier: Identifier): Identifier {
        val table = dynamoDB.getTable(tableName)

        val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
            .withPrimaryKey("PK", identifier.id, "SK", identifier.id)


        table.deleteItem(deleteItemSpec)
        return identifier

    }

    override fun create(t: Workspace): Workspace {
        TODO("Not yet implemented")
    }

    override fun update(t: Workspace): Workspace {
        TODO("Not yet implemented")
    }

    fun addNodePathToHierarchy(workspaceID: String, nodePath: String){

        LOG.info("$workspaceID, $nodePath")
        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedAt"] = System.currentTimeMillis()
        expressionAttributeValues[":nodePath"] = mutableListOf(nodePath)
        expressionAttributeValues[":empty_list"] = mutableListOf<String>()

        val updateExpression = "set nodeHierarchyInformation = list_append(if_not_exists(nodeHierarchyInformation, :empty_list), :nodePath), updatedAt = :updatedAt"

        try {
            UpdateItemSpec().withPrimaryKey("PK", workspaceID, "SK", workspaceID)
                    .withUpdateExpression(updateExpression)
                    .withValueMap(expressionAttributeValues)
                    .withConditionExpression("attribute_exists(PK) and attribute_exists(SK)")
                    .let {
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

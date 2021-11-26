package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
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
        return try {
            return mapper.load(Workspace::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    override fun delete(identifier: Identifier): Identifier? {
        val table = dynamoDB.getTable(tableName)

        val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
            .withPrimaryKey("PK", identifier.id, "SK", identifier.id)

        return try {
            table.deleteItem(deleteItemSpec)
            identifier
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
    }

    override fun create(t: Workspace): Workspace {
        TODO("Not yet implemented")
    }

    override fun update(t: Workspace): Workspace {
        TODO("Not yet implemented")
    }

    fun getWorkspaceData(workspaceIDList: List<String>): MutableMap<String, Workspace?>? {
        val workspaceMap: MutableMap<String, Workspace?> = mutableMapOf()
        return try {
            for (workspaceID in workspaceIDList) {
                val workspace: Workspace? = mapper.load(Workspace::class.java, workspaceID, workspaceID, dynamoDBMapperConfig)
                workspaceMap[workspaceID] = workspace
            }
            return workspaceMap
        } catch (e: Exception) {
            LOG.info(e)
            null
        }
        TODO("we also need to have some sort of filter which filters out all the non-workspace ids")
    }

    companion object {
        private val LOG = LogManager.getLogger(WorkspaceRepository::class.java)
    }
}

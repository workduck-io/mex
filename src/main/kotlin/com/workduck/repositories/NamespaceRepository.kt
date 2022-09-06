package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.serverless.utils.Constants
import com.workduck.models.Identifier
import com.workduck.models.ItemType
import com.workduck.models.Namespace
import org.apache.logging.log4j.LogManager


class NamespaceRepository(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Namespace> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }


    override fun get(pkIdentifier: Identifier, skIdentifier: Identifier, clazz: Class<Namespace>): Namespace? {
        return mapper.load(clazz, pkIdentifier, skIdentifier.id, dynamoDBMapperConfig)
    }

    override fun create(t: Namespace): Namespace {
        TODO("Not yet implemented")
    }

    override fun update(t: Namespace): Namespace {
        TODO("Not yet implemented")
    }


    override fun delete(pkIdentifier: Identifier, skIdentifier: Identifier): Identifier {
        TODO("Using deleteComment instead")
    }


    fun getAllNamespaceData(workspaceID: String): List<Namespace> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Namespace.name.uppercase())


        return DynamoDBQueryExpression<Namespace>().query(keyConditionExpression = "PK = :PK and begins_with(SK, :SK)",
                expressionAttributeValues = expressionAttributeValues).let {
            mapper.query(Namespace::class.java, it, dynamoDBMapperConfig) }
    }

    fun checkIfNamespaceNameExists(workspaceID: String, namespaceName: String) : Boolean {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Namespace.name.uppercase())
        expressionAttributeValues[":name"] = AttributeValue(namespaceName)



        return DynamoDBQueryExpression<Namespace>().query(keyConditionExpression = "PK = :PK and begins_with(SK, :SK)",
                filterExpression = "namespaceName = :name" ,expressionAttributeValues = expressionAttributeValues).let {
            mapper.query(Namespace::class.java, it, dynamoDBMapperConfig).isNotEmpty()
                }

    }


    fun isNamespacePublic(namespaceID: String, workspaceID: String) : Boolean{
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue().withS(namespaceID)

        return DynamoDBQueryExpression<Namespace>().query(
                keyConditionExpression = "PK = :PK and SK = :SK", projectionExpression = "publicAccess",
                expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(Namespace::class.java, it, dynamoDBMapperConfig).let { list ->
                when(list.size){
                     0 -> throw IllegalArgumentException("Invalid NamespaceID")
                    else -> list.first().publicAccess
                }
            }
        }

    }

    fun setPublicAccessValue(namespaceID: String, workspaceID: String, publicAccess: Int) {
        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":publicAccess"] = publicAccess

        return UpdateItemSpec().update(
                pk = workspaceID, sk = namespaceID, updateExpression = "SET publicAccess = :publicAccess",
                expressionAttributeValues = expressionAttributeValues, conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"
        ).let {
            table.updateItem(it)
        }
    }

    fun getPublicNamespace(namespaceID: String) : Namespace {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue(namespaceID)
        expressionAttributeValues[":PK"] = AttributeValue("WORKSPACE")
        expressionAttributeValues[":true"] = AttributeValue().withN("1")
        //expressionAttributeValues[":itemStatus"] = AttributeValue(ItemStatus.ACTIVE.name)

        return DynamoDBQueryExpression<Namespace>().queryWithIndex(index = "SK-PK-Index", keyConditionExpression = "SK = :SK  and begins_with(PK, :PK)",
                filterExpression = "publicAccess = :true", expressionAttributeValues = expressionAttributeValues).let {
            mapper.query(Namespace::class.java, it, dynamoDBMapperConfig).let { list ->
                if(list.isNotEmpty()) list[0]
                else throw NoSuchElementException("Requested Resource Not Found")
            }
        }
    }


    fun addNodePathToHierarchy(workspaceID: String, namespaceID: String, path: String){

        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()
        expressionAttributeValues[":path"] = mutableListOf(path)
        expressionAttributeValues[":empty_list"] = mutableListOf<String>()

        val updateExpression = "set nodeHierarchyInformation = list_append(if_not_exists(nodeHierarchyInformation, :empty_list), :path), updatedAt = :updatedAt"

        try {
            UpdateItemSpec().update(pk = workspaceID, sk = namespaceID, updateExpression = updateExpression,
                    conditionExpression = "attribute_exists(PK) and attribute_exists(SK)", expressionAttributeValues = expressionAttributeValues).let {
                table.updateItem(it)
            }

        }catch (e: ConditionalCheckFailedException){
            LOG.warn("Invalid WorkspaceID : $workspaceID or NamespaceID : $namespaceID")
        }

    }

    companion object {
        private val LOG = LogManager.getLogger(NamespaceRepository::class.java)
    }

}

package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.workduck.models.Identifier
import com.workduck.models.ItemType
import com.workduck.models.Namespace


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
        TODO("Not yet implemented")
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


    fun getNamespaceData(namespaceIDList: List<String>, workspaceID: String): MutableList<MutableMap<String, AttributeValue>> {
        val namespaceMap: MutableMap<String, Namespace?> = mutableMapOf()

        val keysAndAttributes = TableKeysAndAttributes(tableName)
        for(namespaceID in namespaceIDList){
            keysAndAttributes.addHashAndRangePrimaryKey("PK", workspaceID, "SK", namespaceID)
        }

        val spec = BatchGetItemSpec().withTableKeyAndAttributes(keysAndAttributes)
        val itemOutcome = dynamoDB.batchGetItem(spec)

        return itemOutcome.batchGetItemResult.responses[tableName]!!

    }

}

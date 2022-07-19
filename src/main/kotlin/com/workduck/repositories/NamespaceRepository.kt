package com.workduck.repositories


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.models.Identifier
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

    fun getNamespaceData(namespaceIDList: List<String>): MutableMap<String, Namespace?>? {
        val namespaceMap: MutableMap<String, Namespace?> = mutableMapOf()

        for (namespaceID in namespaceIDList) {
            val namespace: Namespace? = mapper.load(Namespace::class.java, namespaceID, namespaceID, dynamoDBMapperConfig)
            namespaceMap[namespaceID] = namespace
        }
        return namespaceMap

        TODO("we also need to have some sort of filter which filters out all the non-namespace ids")
        TODO("this code can be reused for similar workspace functionality")
    }

}

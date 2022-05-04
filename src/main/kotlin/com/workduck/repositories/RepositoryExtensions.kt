package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ReturnValue
import com.workduck.models.Tag


fun UpdateItemSpec.update(pk : String, sk: String, updateExpression: String,
                          expressionAttributeValues : MutableMap<String, Any>, conditionExpression: String) : UpdateItemSpec{

    return this.withPrimaryKey("PK", pk, "SK", sk)
            .withUpdateExpression(updateExpression)
            .withValueMap(expressionAttributeValues)
            .withConditionExpression(conditionExpression)
}


fun UpdateItemSpec.updateWithReturnValues(pk : String, sk: String, updateExpression: String,
                          expressionAttributeValues : MutableMap<String, Any>, conditionExpression: String, returnValue: ReturnValue) : UpdateItemSpec{

    return this.withPrimaryKey("PK", pk, "SK", sk)
            .withUpdateExpression(updateExpression)
            .withValueMap(expressionAttributeValues)
            .withConditionExpression(conditionExpression)
            .withReturnValues(returnValue)
}


fun <T> DynamoDBQueryExpression<T>.queryWithProjection(keyConditionExpression: String, filterExpression: String,
                                  projectionExpression: String, expressionAttributeValues: MutableMap<String, AttributeValue>):DynamoDBQueryExpression<T> {

    return this.withKeyConditionExpression(keyConditionExpression)
            .withFilterExpression(filterExpression)
            .withProjectionExpression(projectionExpression)
            .withExpressionAttributeValues(expressionAttributeValues)

}
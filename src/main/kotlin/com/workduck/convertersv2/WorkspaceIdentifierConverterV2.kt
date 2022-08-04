package com.workduck.convertersv2

import com.workduck.models.WorkspaceIdentifier
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class WorkspaceIdentifierConverterV2 : AttributeConverter<WorkspaceIdentifier> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(workspaceIdentifierObject: WorkspaceIdentifier): AttributeValue {
        return AttributeValue.builder().s(workspaceIdentifierObject.id).build()
    }

    override fun transformTo(workspaceID: AttributeValue): WorkspaceIdentifier {
        return WorkspaceIdentifier(workspaceID.s())
    }

    override fun type(): EnhancedType<WorkspaceIdentifier> {
        return EnhancedType.of(WorkspaceIdentifier::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}
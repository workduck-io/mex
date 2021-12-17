package com.serverless.sqsEventHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.workduck.utils.Helper

class SQSEventHandler : RequestHandler<SQSEvent, Void> {
    override fun handleRequest(event: SQSEvent, context: Context): Void? {
        for (msg in event.records) {
            println(msg.body)
            //val newImage = Helper.objectMapper.readValue(msg.body)
        }
        return null
    }
}

/*

Records: [
    {
      messageId: 'd7b26848-4337-4fb7-9c61-4a1c4bfb7599',
      receiptHandle: 'AQEB7zUd2LcY56pu9DFOGbizdfQXoduJoOOWmAaXvdJ6kM/M8v5oeRCdQ+TghX7pqtBW9e1dwWbo3F9BeJKrrom9OQqUBNUm2pbB68xfAn5wFHqqv8ueMKzKBMgT0x4QRlBfHoTxRqEh34hrxMojc2cXI4Po4Bt69KKMkuteSp2ya222BUKVR5u/150jLh/QSvQvpmfiUhZ9c7Sg3Qv7PeB5F7XT53pNXIHDE1ZWoe47Pr89CVOVVGj294CzJlXMyCyeI5pOD7dSPRkIGaKnKTOa3DXyC9dvBNGeTZMyUzEUHt60gXnJroSit6bxGxUQSXqGW8mliUZHPtz6wNYCme/r3eblhjaPn68l1vJgBBLK1TPa97Nt+DcxEn7AnRAtg34MMD++LNYSbThBYub4NDxptQ==',
      body: '{"ApproximateCreationDateTime":1639733545,"Keys":{"SK":{"S":"NODE1"},"PK":{"S":"NODE1"}},"NewImage":{"itemType":{"S":"Node"},"nodeData":{"M":{"1234":{"S":"{\\"id\\":\\"1234\\",\\"parentID\\":null,\\"content\\":null,\\"children\\":[{\\"id\\":\\"sampleChildID\\",\\"parentID\\":null,\\"content\\":\\"sample child content\\",\\"children\\":null,\\"elementType\\":\\"paragraph\\",\\"properties\\":{\\"bold\\":true,\\"italic\\":true},\\"createdBy\\":null,\\"lastEditedBy\\":null,\\"createdAt\\":null,\\"updatedAt\\":null}],\\"elementType\\":\\"paragraph\\",\\"properties\\":null,\\"createdBy\\":\\"Varun\\",\\"lastEditedBy\\":\\"Varun\\",\\"createdAt\\":1639733543249,\\"updatedAt\\":1639733543249}"},"ABC":{"S":"{\\"id\\":\\"ABC\\",\\"parentID\\":null,\\"content\\":null,\\"children\\":[{\\"id\\":\\"sampleChildID\\",\\"parentID\\":null,\\"content\\":\\"sample child content\\",\\"children\\":null,\\"elementType\\":\\"paragraph\\",\\"properties\\":{\\"bold\\":true,\\"italic\\":true},\\"createdBy\\":null,\\"lastEditedBy\\":null,\\"createdAt\\":null,\\"updatedAt\\":null}],\\"elementType\\":\\"paragraph\\",\\"properties\\":null,\\"createdBy\\":\\"Varun\\",\\"lastEditedBy\\":\\"Varun\\",\\"createdAt\\":1639733543249,\\"updatedAt\\":1639733543249}"},"sampleParentID":{"S":"{\\"id\\":\\"sampleParentID\\",\\"parentID\\":null,\\"content\\":null,\\"children\\":[{\\"id\\":\\"sampleChildID\\",\\"parentID\\":null,\\"content\\":\\"sample child content 1\\",\\"children\\":null,\\"elementType\\":\\"paragraph\\",\\"properties\\":{\\"bold\\":true,\\"italic\\":true},\\"createdBy\\":null,\\"lastEditedBy\\":null,\\"createdAt\\":null,\\"updatedAt\\":null}],\\"elementType\\":\\"paragraph\\",\\"properties\\":null,\\"createdBy\\":\\"Varun\\",\\"lastEditedBy\\":\\"Varun\\",\\"createdAt\\":1639733543249,\\"updatedAt\\":1639733543249}"}}},"publicAccess":{"N":"0"},"namespaceIdentifier":{"S":"NAMESPACE1"},"workspaceIdentifier":{"S":"WORKSPACE1"},"AK":{"S":"WORKSPACE1#NAMESPACE1"},"nodeDataOrder":{"L":[{"S":"sampleParentID"},{"S":"1234"},{"S":"ABC"}]},"version":{"N":"1"},"lastEditedBy":{"S":"Varun"},"createdAt":{"N":"1639733543249"},"createdBy":{"S":"Varun"},"itemStatus":{"S":"ACTIVE"},"SK":{"S":"NODE1"},"PK":{"S":"NODE1"},"nodeVersionCount":{"N":"0"},"updatedAt":{"N":"1639733543249"}},"SequenceNumber":"61350200000000016690767527","SizeBytes":1593,"StreamViewType":"NEW_AND_OLD_IMAGES"}',
      attributes: [Object],
      messageAttributes: {},
      md5OfBody: '93a296f24c8d5dc16bd4cb750ad688c2',
      eventSource: 'aws:sqs',
      eventSourceARN: 'arn:aws:sqs:us-east-1:418506370286:DDBStreamLambdaQueueTest',
      awsRegion: 'us-east-1'
    }
  ]
}
 */

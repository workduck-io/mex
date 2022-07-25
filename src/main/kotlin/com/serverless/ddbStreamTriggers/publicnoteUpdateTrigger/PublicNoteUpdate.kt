package com.serverless.ddbStreamTriggers.publicnoteUpdateTrigger

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Node
import com.workduck.repositories.Cache
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager


class PublicNoteUpdate : RequestHandler<DynamodbEvent, Void> {
//    private val publicNodeCache: Cache = Cache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT"))
    private val cacheExpTimeInSeconds: Long = 900

    fun mapToJson(keyValueMap: Map<String, AttributeValue>): Map<String?, Any?> {
        val finalKeyValueMap: MutableMap<String?, Any?> = mutableMapOf()
        for ((key, value) in keyValueMap.entries) {
            if (value.n != null) {
                finalKeyValueMap[key] = value.n
            } else if (value.m != null) {
                finalKeyValueMap[key] = mapToJson(value.m)
            } else if (value.s != null) {
                finalKeyValueMap[key] = value.s
            } else if (value.l != null) {
                val mutableList = mutableListOf<Any>()
                for (listValue in value.l) {
                    mutableList.add(listValue.s)
                }
                finalKeyValueMap[key] = mutableList
            } else if (value.bool != null) {
                finalKeyValueMap[key] = value.bool
            } else {
                LOG.error("Unhandled value type $key  $value")
                throw Error("Unhandled value type")
            }
        }
        return finalKeyValueMap
    }

    override fun handleRequest(dynamodbEvent: DynamodbEvent?, context: Context): Void? {
        try {
            if(dynamodbEvent == null || dynamodbEvent.records == null) return null /* will be the case when warmup lambda calls it */

            for (record in dynamodbEvent.records) {
                val newImage = record.dynamodb.newImage
                val nodeID = record.dynamodb.newImage["SK"]?.s ?: throw Exception("Invalid Record. NodeID not available")
                val publicAccessValue = record.dynamodb.newImage["publicAccess"]?.n?.toBoolean()
                val jsonResult = mapToJson(newImage).toMutableMap()
                LOG.debug("newImage ${record.dynamodb.newImage.toString()}")
                LOG.debug("jsonResult $jsonResult")

                if(jsonResult["publicAccess"].toString() == "1") jsonResult["publicAccess"] = "true"
                else if (jsonResult["publicAccess"].toString() == "0") jsonResult["publicAccess"] = "false"

                val jsonString = ObjectMapper().writeValueAsString(jsonResult)

                LOG.debug("jsonString $jsonString")

//                val nodeObject : Node = Helper.objectMapper.readValue(jsonString)
                val nodeObject : Node = Helper.objectMapper.convertValue(jsonString, Node::class.java)

                LOG.debug("nodeObject ${nodeObject.toString()}")

                // Check for public access update for the node
                if(publicAccessValue!!) {
//                    publicNodeCache.setEx(nodeID.toString(), cacheExpTimeInSeconds, nodeObject.toString())
//                    val nodeObjectFromCache = publicNodeCache.get(nodeID.toString())
//                    LOG.debug(nodeObjectFromCache.toString())
                }
            }
        } catch (exception: Exception) {
            LOG.debug(exception.message.toString())
        }

        return null
    }

    companion object {
        private val LOG = LogManager.getLogger(PublicNoteUpdate::class.java)
    }

}

fun main() {

    val x = """
      {
        "itemType": {
            "s": "Node"
        },
        "nodeData": {
            "m": {
                "TEMP_64nyx": {
                    "s": "{\"id\":\"TEMP_64nyx\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_yHw6C\",\"content\":\"feeffefefef\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658592089817,\"updatedAt\":1658592089817}"
                },
                "TEMP_6YzEm": {
                    "s": "{\"id\":\"TEMP_6YzEm\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_YXJU6\",\"content\":\"pklpkl\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658507527288,\"updatedAt\":1658507527288}"
                },
                "TEMP_QFLnN": {
                    "s": "{\"id\":\"TEMP_QFLnN\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_FH6dd\",\"content\":\"efkeopkepogkpoegk\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658507481227,\"updatedAt\":1658507481227}"
                },
                "TEMP_ejpqM": {
                    "s": "{\"id\":\"TEMP_ejpqM\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_P63eU\",\"content\":\"eekgopkgprkg\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658507522083,\"updatedAt\":1658507522083}"
                },
                "TEMP_irhry": {
                    "s": "{\"id\":\"TEMP_irhry\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_Fijc7\",\"content\":\"\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658592089817,\"updatedAt\":1658592089817}"
                },
                "TEMP_UiMhx": {
                    "s": "{\"id\":\"TEMP_UiMhx\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_F9r6z\",\"content\":\"jhiuh\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658592089817,\"updatedAt\":1658592089817}"
                },
                "TEMP_U8nxh": {
                    "s": "{\"id\":\"TEMP_U8nxh\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_RRTdD\",\"content\":\"\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658592089817,\"updatedAt\":1658592089817}"
                },
                "TEMP_LzK7w": {
                    "s": "{\"id\":\"TEMP_LzK7w\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_ikiBB\",\"content\":\"wfoerjijrfgorjego\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658506184153,\"updatedAt\":1658506184153}"
                },
                "TEMP_gpCH6": {
                    "s": "{\"id\":\"TEMP_gpCH6\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_aCK6b\",\"content\":\"efefef\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658592089817,\"updatedAt\":1658592089817}"
                },
                "TEMP_6tFhW": {
                    "s": "{\"id\":\"TEMP_6tFhW\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_tc4mB\",\"content\":\"ekfpwokopkgopkg\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658555871861,\"updatedAt\":1658555871861}"
                },
                "TEMP_TyNEb": {
                    "s": "{\"id\":\"TEMP_TyNEb\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_Wzftr\",\"content\":\"ijfriowejrjgorjegrjegoerjgioerjg\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658555871861,\"updatedAt\":1658560603676}"
                },
                "TEMP_gkQ8b": {
                    "s": "{\"id\":\"TEMP_gkQ8b\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_TJYmD\",\"content\":\"wefeowfjweiojfieowjfoijefwij\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658506115256,\"updatedAt\":1658506115256}"
                },
                "TEMP_KHcUD": {
                    "s": "{\"id\":\"TEMP_KHcUD\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_AJtjh\",\"content\":\"efjoej\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658506416791,\"updatedAt\":1658506416791}"
                },
                "TEMP_Hidi4": {
                    "s": "{\"id\":\"TEMP_Hidi4\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_y3WLX\",\"content\":\"efe\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658505973640,\"updatedAt\":1658506050045}"
                },
                "TEMP_Bapti": {
                    "s": "{\"id\":\"TEMP_Bapti\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_aUgtf\",\"content\":\"iejwfojewifjeoijf\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658506049937,\"updatedAt\":1658506115385}"
                },
                "TEMP_EbBJa": {
                    "s": "{\"id\":\"TEMP_EbBJa\",\"content\":\"\",\"children\":[{\"id\":\"TEMP_AVCgF\",\"content\":\"\",\"children\":null,\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":null,\"lastEditedBy\":null,\"createdAt\":null,\"updatedAt\":null}],\"elementType\":\"p\",\"properties\":null,\"elementMetadata\":null,\"createdBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"lastEditedBy\":\"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\"createdAt\":1658507527288,\"updatedAt\":1658507527288}"
                }
            }
        },
        "publicAccess": {
            "n": "0"
        },
        "namespaceIdentifier": {
            "s": "NAMESPACE1"
        },
        "AK": {
            "s": "WORKSPACE_86XnEGaVfThbALRn9YDt3#NAMESPACE1"
        },
        "title": {
            "s": "Untitled"
        },
        "version": {
            "n": "18"
        },
        "lastEditedBy": {
            "s": "f16f48ad-87b4-431d-b453-53ad6dbcca47"
        },
        "tags": {
            "l": []
        },
        "dataOrder": {
            "l": [
                {
                    "s": "TEMP_Hidi4"
                },
                {
                    "s": "TEMP_Bapti"
                },
                {
                    "s": "TEMP_gkQ8b"
                },
                {
                    "s": "TEMP_LzK7w"
                },
                {
                    "s": "TEMP_KHcUD"
                },
                {
                    "s": "TEMP_QFLnN"
                },
                {
                    "s": "TEMP_ejpqM"
                },
                {
                    "s": "TEMP_EbBJa"
                },
                {
                    "s": "TEMP_6YzEm"
                },
                {
                    "s": "TEMP_6tFhW"
                },
                {
                    "s": "TEMP_TyNEb"
                },
                {
                    "s": "TEMP_gpCH6"
                },
                {
                    "s": "TEMP_64nyx"
                },
                {
                    "s": "TEMP_irhry"
                },
                {
                    "s": "TEMP_UiMhx"
                },
                {
                    "s": "TEMP_U8nxh"
                }
            ]
        },
        "createdAt": {
            "n": "1658502671583"
        },
        "createdBy": {
            "s": "f16f48ad-87b4-431d-b453-53ad6dbcca47"
        },
        "itemStatus": {
            "s": "ACTIVE"
        },
        "SK": {
            "s": "NODE_Fc9cnNB6rGeACCfAFYJaj"
        },
        "PK": {
            "s": "WORKSPACE_86XnEGaVfThbALRn9YDt3"
        },
        "nodeVersionCount": {
            "n": "0"
        },
        "updatedAt": {
            "n": "1658592089817"
        }
    }  
    """.trimIndent()
    val m : Map<String, AttributeValue> = Helper.objectMapper.readValue(x)
    val res = PublicNoteUpdate().mapToJson(m)
    val temp = "{\n" +
            "    \"itemType\": \"Node\",\n" +
            "    \"nodeData\": {\n" +
            "        \"TEMP_U8nxh\": \"{\\\"id\\\":\\\"TEMP_U8nxh\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_RRTdD\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":null,\\\"elementType\\\":\\\"p\\\",\\\"properties\\\":null,\\\"elementMetadata\\\":null,\\\"createdBy\\\":null,\\\"lastEditedBy\\\":null,\\\"createdAt\\\":null,\\\"updatedAt\\\":null}],\\\"elementType\\\":\\\"p\\\",\\\"properties\\\":null,\\\"elementMetadata\\\":null,\\\"createdBy\\\":\\\"f16f48ad-87b4-431d-b453-53ad6dbcca47\\\",\\\"lastEditedBy\\\":\\\"f16f48ad-87b4-431d-b453-53ad6dbcca47\\\",\\\"createdAt\\\":1658592089817,\\\"updatedAt\\\":1658592089817}\",\n" +
            "        \"TEMP_Hidi4\": \"{\\\"id\\\":\\\"TEMP_Hidi4\\\",\\\"content\\\":\\\"\\\",\\\"children\\\":[{\\\"id\\\":\\\"TEMP_y3WLX\\\",\\\"content\\\":\\\"efe. \\\",\\\"children\\\":null,\\\"elementType\\\":\\\"p\\\",\\\"properties\\\":null,\\\"elementMetadata\\\":null,\\\"createdBy\\\":null,\\\"lastEditedBy\\\":null,\\\"createdAt\\\":null,\\\"updatedAt\\\":null}],\\\"elementType\\\":\\\"p\\\",\\\"properties\\\":null,\\\"elementMetadata\\\":null,\\\"createdBy\\\":\\\"f16f48ad-87b4-431d-b453-53ad6dbcca47\\\",\\\"lastEditedBy\\\":\\\"f16f48ad-87b4-431d-b453-53ad6dbcca47\\\",\\\"createdAt\\\":1658505973640,\\\"updatedAt\\\":1658764725698}\"\n" +
            "    },\n" +
            "    \"publicAccess\": \"true\",\n" +
            "    \"namespaceIdentifier\": \"NAMESPACE1\",\n" +
            "    \"AK\": \"WORKSPACE_86XnEGaVfThbALRn9YDt3#NAMESPACE1\",\n" +
            "    \"title\": \"Untitled\",\n" +
            "    \"version\": \"20\",\n" +
            "    \"lastEditedBy\": \"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\n" +
            "    \"tags\": [],\n" +
            "    \"dataOrder\": [\n" +
            "        \"TEMP_Hidi4\",\n" +
            "        \"TEMP_U8nxh\"\n" +
            "    ],\n" +
            "    \"createdAt\": \"1658502671583\",\n" +
            "    \"createdBy\": \"f16f48ad-87b4-431d-b453-53ad6dbcca47\",\n" +
            "    \"itemStatus\": \"ACTIVE\",\n" +
            "    \"SK\": \"NODE_Fc9cnNB6rGeACCfAFYJaj\",\n" +
            "    \"PK\": \"WORKSPACE_86XnEGaVfThbALRn9YDt3\",\n" +
            "    \"nodeVersionCount\": \"0\",\n" +
            "    \"updatedAt\": \"1658764725676\"\n" +
            "}\n"
    val node : Node = Helper.objectMapper.convertValue(temp, Node::class.java)

    print(node)
}
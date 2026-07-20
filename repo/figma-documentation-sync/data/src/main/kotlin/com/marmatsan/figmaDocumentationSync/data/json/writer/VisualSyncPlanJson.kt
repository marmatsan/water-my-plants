package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlan
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlanBody
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncDecision
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncIdentity
import com.marmatsan.figmaDocumentationSync.domain.port.writer.VisualSyncPlanHasher
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Canonical hasher and filesystem JSON adapter for [VisualSyncPlan]. */
class VisualSyncPlanJson : VisualSyncPlanHasher {
    override fun hash(
        body: VisualSyncPlanBody
    ): String = Sha256Hash.of(CanonicalJson.stringify(body.toJson()))

    fun write(
        plan: VisualSyncPlan,
        outputPath: String
    ) {
        val output = Path.of(outputPath)
        output.parent?.let(Files::createDirectories)
        val body = plan.body.toJson().toMutableMap()
        body["planHash"] = JsonPrimitive(plan.planHash)
        Files.writeString(
            output,
            prettyJson.encodeToString(
                JsonObject.serializer(),
                JsonObject(body)
            ) + System.lineSeparator()
        )
    }

    fun read(
        inputPath: String
    ): VisualSyncPlan {
        val source = Json.parseToJsonElement(Files.readString(Path.of(inputPath)).removePrefix(UTF8_BOM)).jsonObject
        val identity = source.getValue("identity").jsonObject
        val decisionValue = source.getValue("decision").jsonPrimitive.content
        val body = VisualSyncPlanBody(
            schemaVersion = source.getValue("schemaVersion").jsonPrimitive.int,
            decision = VisualSyncDecision.entries.singleOrNull { decision -> decision.wireValue == decisionValue }
                ?: throw IllegalArgumentException("Unknown visual sync decision '$decisionValue'."),
            reason = source.getValue("reason").jsonPrimitive.content,
            requiresVisualWrite = source.getValue("requiresVisualWrite").jsonPrimitive.boolean,
            requiresMetadataWrite = source.getValue("requiresMetadataWrite").jsonPrimitive.boolean,
            executionScopes = source.getValue("executionScopes").jsonArray.map { value -> value.jsonPrimitive.content },
            identity = VisualSyncIdentity(
                modelHash = identity.getValue("modelHash").jsonPrimitive.content,
                writerHash = identity.getValue("writerHash").jsonPrimitive.content,
                transportHash = identity.getValue("transportHash").jsonPrimitive.content,
                writerScopeFingerprintSchemaVersion =
                    identity.getValue("writerScopeFingerprintSchemaVersion").jsonPrimitive.int
            ),
            manifestHash = source.getValue("manifestHash").jsonPrimitive.content
        )
        val plan = VisualSyncPlan(
            body = body,
            planHash = source.getValue("planHash").jsonPrimitive.content
        )
        require(
            plan.planHash == hash(
                body = body
            )
        ) { "Visual sync plan hash mismatch: ${plan.planHash} != ${hash(
            body = body
        )}." }
        return plan
    }

    private fun VisualSyncPlanBody.toJson(): JsonObject = buildJsonObject {
        put(
            "schemaVersion",
            schemaVersion
        )
        put(
            "decision",
            decision.wireValue
        )
        put(
            "reason",
            reason
        )
        put(
            "requiresVisualWrite",
            requiresVisualWrite
        )
        put(
            "requiresMetadataWrite",
            requiresMetadataWrite
        )
        put(
            "executionScopes",
            JsonArray(executionScopes.map(::JsonPrimitive))
        )
        put(
            "identity",
            buildJsonObject {
                put(
                    "modelHash",
                    identity.modelHash
                )
                put(
                    "writerHash",
                    identity.writerHash
                )
                put(
                    "transportHash",
                    identity.transportHash
                )
                put(
                    "writerScopeFingerprintSchemaVersion",
                    identity.writerScopeFingerprintSchemaVersion
                )
            }
        )
        put(
            "manifestHash",
            manifestHash
        )
    }

    private companion object {
        const val UTF8_BOM = "\uFEFF"
        val prettyJson = Json { prettyPrint = true }
    }
}

package com.marmatsan.figmaDesignSync.data.json.writer

import com.marmatsan.figmaDesignSync.data.hash.Sha256Hash
import com.marmatsan.figmaDesignSync.data.json.CanonicalJson
import com.marmatsan.figmaDesignSync.domain.model.writer.VisualSyncPlan
import com.marmatsan.figmaDesignSync.domain.model.writer.VisualSyncPlanBody
import com.marmatsan.figmaDesignSync.domain.port.writer.VisualSyncPlanHasher
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Canonical hasher and filesystem JSON adapter for [VisualSyncPlan]. */
class VisualSyncPlanJson : VisualSyncPlanHasher {
    override fun hash(body: VisualSyncPlanBody): String = Sha256Hash.of(CanonicalJson.stringify(body.toJson()))

    fun write(plan: VisualSyncPlan, outputPath: String) {
        val output = Path.of(outputPath)
        output.parent?.let(Files::createDirectories)
        val body = plan.body.toJson().toMutableMap()
        body["planHash"] = JsonPrimitive(plan.planHash)
        Files.writeString(
            output,
            prettyJson.encodeToString(JsonObject.serializer(), JsonObject(body)) + System.lineSeparator()
        )
    }

    private fun VisualSyncPlanBody.toJson(): JsonObject = buildJsonObject {
        put("schemaVersion", schemaVersion)
        put("decision", decision.wireValue)
        put("reason", reason)
        put("requiresVisualWrite", requiresVisualWrite)
        put("requiresMetadataWrite", requiresMetadataWrite)
        put("executionScopes", JsonArray(executionScopes.map(::JsonPrimitive)))
        put(
            "identity",
            buildJsonObject {
                put("modelHash", identity.modelHash)
                put("writerHash", identity.writerHash)
                put("transportHash", identity.transportHash)
                put("writerScopeFingerprintSchemaVersion", identity.writerScopeFingerprintSchemaVersion)
            }
        )
        put("manifestHash", manifestHash)
    }

    private companion object {
        val prettyJson = Json { prettyPrint = true }
    }
}

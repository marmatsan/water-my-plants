package com.marmatsan.figmaDesignSync.data.figma.sync

import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaChangeImpact
import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaImpact
import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDesignSync.domain.model.sync.OfficialFigmaSyncScope
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.tatarka.inject.annotations.Inject

/** Filesystem JSON adapter for the official Figma Sync scope and runner identities. */
@Inject
class OfficialFigmaSyncScopeJson {
    fun readChangeImpact(sourcePath: String): FigmaChangeImpact {
        val source = readObject(Path.of(sourcePath), "Figma change-impact report")
        return FigmaChangeImpact(
            scope = source.requiredEnum("scope", FigmaVerificationScope.entries, FigmaVerificationScope::wireValue),
            impact = source.requiredEnum("figmaImpact", FigmaImpact.entries, FigmaImpact::wireValue),
            affectedVisualTargets = source.requiredStringList("affectedVisualTargets"),
            comparisonBase = source.optionalString("comparisonBase"),
            changedPaths = source.requiredStringList("changedPaths")
        )
    }

    fun read(sourcePath: String): OfficialFigmaSyncScope {
        val source = readObject(Path.of(sourcePath), "official Figma Sync scope")
        return OfficialFigmaSyncScope(
            scope = source.requiredEnum("scope", FigmaVerificationScope.entries, FigmaVerificationScope::wireValue),
            figmaImpact = source.requiredEnum("figmaImpact", FigmaImpact.entries, FigmaImpact::wireValue),
            affectedVisualTargets = source.requiredStringList("affectedVisualTargets"),
            comparisonBase = source.optionalString("comparisonBase"),
            gitSha = source.requiredString("gitSha"),
            modelHash = source.optionalString("modelHash"),
            writerHash = source.optionalString("writerHash"),
            transportHash = source.optionalString("transportHash"),
            targetFingerprints = source.optionalStringMap("targetFingerprints"),
            writerScopeFingerprints = source.optionalStringMap("writerScopeFingerprints"),
            writerScopeFingerprintSchemaVersion = source.optionalInt("writerScopeFingerprintSchemaVersion"),
            visualRunnerManifestHash = source.optionalString("visualRunnerManifestHash"),
            metadataRunnerManifestHash = source.optionalString("metadataRunnerManifestHash"),
            visualSyncDecision = source.optionalString("visualSyncDecision"),
            visualSyncPlanHash = source.optionalString("visualSyncPlanHash")
        )
    }

    fun write(scope: OfficialFigmaSyncScope, outputPath: String) {
        val output = Path.of(outputPath)
        output.parent?.let(Files::createDirectories)
        Files.writeString(
            output,
            prettyJson.encodeToString(JsonObject.serializer(), scope.toJson()) + System.lineSeparator()
        )
    }

    fun readRunnerManifests(rootPath: String): List<RunnerManifest> {
        val root = Path.of(rootPath)
        if (!Files.isDirectory(root)) return emptyList()
        return Files.walk(root).use { paths ->
            paths.filter { path -> path.isRegularFile() && path.fileName.toString() == MANIFEST_FILE_NAME }
                .sorted()
                .map { path ->
                    val source = readObject(path, "MCP runner manifest")
                    RunnerManifest(
                        path = path.toAbsolutePath().normalize().toString(),
                        fullVisualSync = source.requiredBoolean("fullVisualSync"),
                        writeMetadata = source.requiredBoolean("writeMetadata"),
                        modelHash = source.requiredString("modelHash"),
                        writerHash = source.requiredString("writerHash"),
                        transportHash = source.requiredString("transportHash"),
                        targetFingerprints = source.requiredStringMap("targetFingerprints"),
                        writerScopeFingerprints = source.requiredStringMap("writerScopeFingerprints"),
                        writerScopeFingerprintSchemaVersion = source.requiredInt("writerScopeFingerprintSchemaVersion"),
                        manifestHash = source.requiredString("manifestHash")
                    )
                }
                .toList()
        }
    }

    fun readVisualSyncPlan(sourcePath: String): VisualSyncPlan {
        val source = readObject(Path.of(sourcePath), "visual sync plan")
        return VisualSyncPlan(
            decision = source.requiredString("decision"),
            planHash = source.requiredString("planHash")
        )
    }

    data class RunnerManifest(
        val path: String,
        val fullVisualSync: Boolean,
        val writeMetadata: Boolean,
        val modelHash: String,
        val writerHash: String,
        val transportHash: String,
        val targetFingerprints: Map<String, String>,
        val writerScopeFingerprints: Map<String, String>,
        val writerScopeFingerprintSchemaVersion: Int,
        val manifestHash: String
    )

    data class VisualSyncPlan(
        val decision: String,
        val planHash: String
    )

    private fun OfficialFigmaSyncScope.toJson() = JsonObject(
        linkedMapOf(
            "scope" to JsonPrimitive(scope.wireValue),
            "figmaImpact" to JsonPrimitive(figmaImpact.wireValue),
            "affectedVisualTargets" to JsonArray(affectedVisualTargets.map(::JsonPrimitive)),
            "comparisonBase" to comparisonBase.toJson(),
            "gitSha" to JsonPrimitive(gitSha),
            "modelHash" to modelHash.toJson(),
            "writerHash" to writerHash.toJson(),
            "transportHash" to transportHash.toJson(),
            "targetFingerprints" to targetFingerprints.toJson(),
            "writerScopeFingerprints" to writerScopeFingerprints.toJson(),
            "writerScopeFingerprintSchemaVersion" to writerScopeFingerprintSchemaVersion.toJson(),
            "visualRunnerManifestHash" to visualRunnerManifestHash.toJson(),
            "metadataRunnerManifestHash" to metadataRunnerManifestHash.toJson(),
            "visualSyncDecision" to visualSyncDecision.toJson(),
            "visualSyncPlanHash" to visualSyncPlanHash.toJson()
        )
    )

    private fun readObject(path: Path, description: String): JsonObject = try {
        require(path.isRegularFile()) { "$description was not found: '$path'." }
        Json.parseToJsonElement(Files.readString(path).removePrefix(UTF8_BOM)).jsonObject
    } catch (exception: Exception) {
        throw IllegalArgumentException("$description '$path' is not valid JSON: ${exception.message}", exception)
    }

    private fun JsonObject.requiredString(name: String): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private fun JsonObject.optionalString(name: String): String? =
        this[name]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content

    private fun JsonObject.requiredBoolean(name: String): Boolean =
        this[name]?.jsonPrimitive?.boolean
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private fun JsonObject.requiredInt(name: String): Int =
        this[name]?.jsonPrimitive?.int
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private fun JsonObject.optionalInt(name: String): Int? =
        this[name]?.takeUnless { it is JsonNull }?.jsonPrimitive?.int

    private fun JsonObject.requiredStringList(name: String): List<String> =
        this[name]?.jsonArray?.map { it.jsonPrimitive.content }
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private fun JsonObject.requiredStringMap(name: String): Map<String, String> =
        this[name]?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content }
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private fun JsonObject.optionalStringMap(name: String): Map<String, String>? =
        this[name]?.takeUnless { it is JsonNull }?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content }

    private fun <T> JsonObject.requiredEnum(
        name: String,
        values: List<T>,
        wireValue: (T) -> String
    ): T {
        val rawValue = requiredString(name)
        return values.singleOrNull { value -> wireValue(value) == rawValue }
            ?: throw IllegalArgumentException("Unsupported '$name' value '$rawValue'.")
    }

    private fun String?.toJson(): JsonElement = this?.let(::JsonPrimitive) ?: JsonNull

    private fun Int?.toJson(): JsonElement = this?.let(::JsonPrimitive) ?: JsonNull

    private fun Map<String, String>?.toJson(): JsonElement = this?.let { values ->
        JsonObject(values.toSortedMap().mapValues { (_, value) -> JsonPrimitive(value) })
    } ?: JsonNull

    private companion object {
        const val MANIFEST_FILE_NAME = "manifest.json"
        const val UTF8_BOM = "\uFEFF"

        val prettyJson = Json {
            prettyPrint = true
            explicitNulls = true
        }
    }
}

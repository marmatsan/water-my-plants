package com.marmatsan.figmaDesignSync.data.figma.sync

import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaChangeImpact
import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaImpact
import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDesignSync.domain.model.sync.OfficialFigmaSyncScope
import com.marmatsan.figmaDesignSync.domain.model.writer.RunnerManifest
import com.marmatsan.figmaDesignSync.data.json.writer.RunnerManifestJson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
        return RunnerManifestJson().readAll(rootPath)
    }

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

    private fun JsonObject.optionalInt(name: String): Int? =
        this[name]?.takeUnless { it is JsonNull }?.jsonPrimitive?.int

    private fun JsonObject.requiredStringList(name: String): List<String> =
        this[name]?.jsonArray?.map { it.jsonPrimitive.content }
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
        const val UTF8_BOM = "\uFEFF"

        val prettyJson = Json {
            prettyPrint = true
            explicitNulls = true
        }
    }
}

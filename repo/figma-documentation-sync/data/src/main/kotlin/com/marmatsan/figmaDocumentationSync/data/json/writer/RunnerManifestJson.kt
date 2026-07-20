package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerManifest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Reads MCP runner manifests and computes their canonical manifest identity. */
class RunnerManifestJson {
    fun hash(
        body: JsonObject
    ): String = Sha256Hash.of(
        value = CanonicalJson.stringify(
            value = body
        )
    )

    fun readAll(
        rootPath: String
    ): List<RunnerManifest> {
        val root = Path.of(
            rootPath
        )
        if (!Files.isDirectory(root)) return emptyList()
        return Files.walk(root).use { paths ->
            paths.filter { path -> path.isRegularFile() && path.fileName.toString() == MANIFEST_FILE_NAME }
                .sorted()
                .map(
                    ::read
                )
                .toList()
        }
    }

    fun read(
        path: Path
    ): RunnerManifest {
        val source = readObject(
            path = path
        )
        return RunnerManifest(
            path = path.toAbsolutePath().normalize().toString(),
            fullVisualSync = source.requiredBoolean("fullVisualSync"),
            writeMetadata = source.requiredBoolean("writeMetadata"),
            modelHash = source.requiredString("modelHash"),
            writerHash = source.requiredString("writerHash"),
            transportHash = source.requiredString("transportHash"),
            targetFingerprints = source.requiredStringMap(
                name = "targetFingerprints"
            ),
            writerScopeFingerprints = source.requiredStringMap(
                name = "writerScopeFingerprints"
            ),
            writerScopeFingerprintSchemaVersion = source.requiredInt("writerScopeFingerprintSchemaVersion"),
            executionScopes = source.requiredStringMap(
                name = "executionScopes"
            ),
            manifestHash = source.requiredString("manifestHash")
        )
    }

    private fun readObject(
        path: Path
    ): JsonObject = try {
        require(path.isRegularFile()) { "MCP runner manifest was not found: '$path'." }
        Json.parseToJsonElement(Files.readString(path).removePrefix(UTF8_BOM)).jsonObject
    } catch (
        exception: Exception
    ) {
        throw IllegalArgumentException(
            "MCP runner manifest '$path' is not valid JSON: ${exception.message}",
            exception
        )
    }

    private fun JsonObject.requiredString(
        name: String
    ): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private fun JsonObject.requiredBoolean(
        name: String
    ): Boolean =
        this[name]?.jsonPrimitive?.boolean
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private fun JsonObject.requiredInt(
        name: String
    ): Int =
        this[name]?.jsonPrimitive?.int
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private fun JsonObject.requiredStringMap(
        name: String
    ): Map<String, String> =
        this[name]?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content }
            ?: throw IllegalArgumentException("JSON is missing required property '$name'.")

    private companion object {
        const val MANIFEST_FILE_NAME = "manifest.json"
        const val UTF8_BOM = "\uFEFF"
    }
}

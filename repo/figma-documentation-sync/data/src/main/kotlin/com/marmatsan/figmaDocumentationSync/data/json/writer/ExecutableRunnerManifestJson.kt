package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerPayloadImage
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Reads, validates, hashes, and writes complete executable runner manifests. */
class ExecutableRunnerManifestJson {
    fun finalizeAndWrite(draft: ExecutableRunnerManifest, outputPath: String): ExecutableRunnerManifest {
        val finalized = draft.copy(
            path = Path.of(outputPath).toAbsolutePath().normalize().toString(),
            manifestHash = hash(draft)
        )
        write(finalized, outputPath)
        return finalized
    }

    fun write(manifest: ExecutableRunnerManifest, outputPath: String) {
        val expectedHash = hash(manifest)
        require(manifest.manifestHash == expectedHash) {
            "MCP manifest hash mismatch: ${manifest.manifestHash} != $expectedHash."
        }
        val output = Path.of(outputPath)
        output.parent?.let(Files::createDirectories)
        Files.writeString(
            output,
            prettyJson.encodeToString(JsonObject.serializer(), manifest.toJson(includeHash = true)) +
                System.lineSeparator()
        )
    }

    fun read(path: String): ExecutableRunnerManifest {
        val normalized = Path.of(path).toAbsolutePath().normalize()
        val source = Json.parseToJsonElement(Files.readString(normalized).removePrefix(UTF8_BOM)).jsonObject
        val manifest = source.toManifest(normalized.toString())
        require(manifest.schemaVersion >= MINIMUM_SCHEMA_VERSION) {
            "Unsupported MCP manifest schema ${manifest.schemaVersion}; expected $MINIMUM_SCHEMA_VERSION or newer."
        }
        manifest.files.forEach { file ->
            require(manifest.fileHashes[file] != null) { "MCP manifest is missing the hash for '$file'." }
        }
        if (manifest.schemaVersion >= 3) {
            listOf(
                "targetFingerprints",
                "writerScopeFingerprints",
                "writerScopeFingerprintSchemaVersion",
                "executionScopes"
            ).forEach { name ->
                require(source[name] != null) {
                    "MCP manifest schema ${manifest.schemaVersion} is missing '$name'."
                }
            }
            val requiredScopes = (manifest.executionScopes.values + "metadata").distinct()
            requiredScopes.forEach { scope ->
                require(manifest.writerScopeFingerprints[scope] != null) {
                    "MCP manifest is missing the writer fingerprint for scope '$scope'."
                }
            }
        }
        require(manifest.transport != "png" || manifest.payloadImage?.sha256 != null) {
            "PNG MCP manifest is missing 'payloadImage.sha256'."
        }
        val expectedHash = Sha256Hash.of(
            CanonicalJson.stringify(JsonObject(source.filterKeys { key -> key != "manifestHash" }))
        )
        require(manifest.manifestHash == expectedHash) {
            "MCP manifest hash mismatch: ${manifest.manifestHash} != $expectedHash."
        }
        return manifest
    }

    fun hash(manifest: ExecutableRunnerManifest): String =
        Sha256Hash.of(CanonicalJson.stringify(manifest.toJson(includeHash = false)))

    private fun ExecutableRunnerManifest.toJson(includeHash: Boolean): JsonObject = buildJsonObject {
        put("schemaVersion", schemaVersion)
        put("mode", mode)
        put("entrypoint", entrypoint)
        put("target", target)
        put("targets", targets.toJsonArray())
        put("writeMetadata", writeMetadata)
        put("transport", transport)
        put("namespace", namespace)
        put("sectionNodeId", sectionNodeId?.let(::JsonPrimitive) ?: JsonNull)
        put("roots", roots.toJsonArray())
        put("allowOfficialSections", allowOfficialSections)
        put("allowPartial", allowPartial)
        put("fullVisualSync", fullVisualSync)
        put("metadataPageId", metadataPageId)
        put("modelPath", modelPath)
        put("scriptPath", scriptPath)
        put("modelHash", modelHash)
        put("gitSha", gitSha)
        put("designModelLength", designModelLength)
        put("scriptLength", scriptLength)
        put("writerHash", writerHash)
        put("transportHash", transportHash)
        put("targetFingerprints", targetFingerprints.toJsonObject())
        put("writerScopeFingerprints", writerScopeFingerprints.toJsonObject())
        put("writerScopeFingerprintSchemaVersion", writerScopeFingerprintSchemaVersion)
        put("executionScopes", executionScopes.toJsonObject())
        put("payloadImage", payloadImage?.toJson() ?: JsonNull)
        put("files", files.toJsonArray())
        put("fileHashes", fileHashes.toJsonObject())
        if (includeHash) put("manifestHash", manifestHash)
    }

    private fun JsonObject.toManifest(path: String): ExecutableRunnerManifest = ExecutableRunnerManifest(
        path = path,
        schemaVersion = requiredInt("schemaVersion"),
        mode = requiredString("mode"),
        entrypoint = requiredString("entrypoint"),
        target = requiredString("target"),
        targets = requiredStringList("targets"),
        writeMetadata = requiredBoolean("writeMetadata"),
        transport = requiredString("transport"),
        namespace = requiredString("namespace"),
        sectionNodeId = this["sectionNodeId"]?.jsonPrimitive?.contentOrNull,
        roots = requiredStringList("roots"),
        allowOfficialSections = requiredBoolean("allowOfficialSections"),
        allowPartial = requiredBoolean("allowPartial"),
        fullVisualSync = requiredBoolean("fullVisualSync"),
        metadataPageId = requiredString("metadataPageId"),
        modelPath = requiredString("modelPath"),
        scriptPath = requiredString("scriptPath"),
        modelHash = requiredString("modelHash"),
        gitSha = requiredString("gitSha"),
        designModelLength = requiredInt("designModelLength"),
        scriptLength = requiredInt("scriptLength"),
        writerHash = requiredString("writerHash"),
        transportHash = requiredString("transportHash"),
        targetFingerprints = stringMapOrEmpty("targetFingerprints"),
        writerScopeFingerprints = stringMapOrEmpty("writerScopeFingerprints"),
        writerScopeFingerprintSchemaVersion = intOrZero("writerScopeFingerprintSchemaVersion"),
        executionScopes = stringMapOrEmpty("executionScopes"),
        payloadImage = this["payloadImage"]?.takeUnless { value -> value === JsonNull }?.jsonObject?.toPayloadImage(),
        files = requiredStringList("files"),
        fileHashes = requiredStringMap("fileHashes"),
        manifestHash = requiredString("manifestHash")
    )

    private fun RunnerPayloadImage.toJson(): JsonObject = buildJsonObject {
        put("fileName", fileName)
        put("byteLength", byteLength)
        put("sha256", sha256)
        put("textKeyword", textKeyword)
    }

    private fun JsonObject.toPayloadImage(): RunnerPayloadImage = RunnerPayloadImage(
        fileName = requiredString("fileName"),
        byteLength = requiredInt("byteLength"),
        sha256 = requiredString("sha256"),
        textKeyword = requiredString("textKeyword")
    )

    private fun JsonObject.requiredString(name: String): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.requiredInt(name: String): Int =
        this[name]?.jsonPrimitive?.int
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.requiredBoolean(name: String): Boolean =
        this[name]?.jsonPrimitive?.boolean
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.requiredStringList(name: String): List<String> =
        this[name]?.jsonArray?.map { value -> value.jsonPrimitive.content }
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.requiredStringMap(name: String): Map<String, String> =
        this[name]?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content }
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.stringMapOrEmpty(name: String): Map<String, String> =
        this[name]?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content }.orEmpty()

    private fun JsonObject.intOrZero(name: String): Int = this[name]?.jsonPrimitive?.int ?: 0

    private fun List<String>.toJsonArray(): JsonArray = JsonArray(map(::JsonPrimitive))

    private fun Map<String, String>.toJsonObject(): JsonObject =
        JsonObject(mapValues { (_, value) -> JsonPrimitive(value) })

    private companion object {
        const val MINIMUM_SCHEMA_VERSION = 2
        const val UTF8_BOM = "\uFEFF"
        val prettyJson = Json { prettyPrint = true }
    }
}

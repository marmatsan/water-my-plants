package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerPayloadImage
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
import java.nio.file.Files
import java.nio.file.Path

/** Reads, validates, hashes, and writes complete executable runner manifests. */
class ExecutableRunnerManifestJson {
    fun finalizeAndWrite(
        draft: ExecutableRunnerManifest,
        outputPath: String,
    ): ExecutableRunnerManifest {
        val finalized =
            draft.copy(
                path =
                    Path
                        .of(
                            outputPath,
                        ).toAbsolutePath()
                        .normalize()
                        .toString(),
                manifestHash =
                    hash(
                        manifest = draft,
                    ),
            )
        write(
            manifest = finalized,
            outputPath = outputPath,
        )
        return finalized
    }

    fun write(
        manifest: ExecutableRunnerManifest,
        outputPath: String,
    ) {
        val expectedHash =
            hash(
                manifest = manifest,
            )
        require(manifest.manifestHash == expectedHash) {
            "MCP manifest hash mismatch: ${manifest.manifestHash} != $expectedHash."
        }
        val output =
            Path.of(
                outputPath,
            )
        output.parent?.let(Files::createDirectories)
        Files.writeString(
            output,
            prettyJson.encodeToString(
                JsonObject.serializer(),
                manifest.toJson(
                    includeHash = true,
                ),
            ) +
                System.lineSeparator(),
        )
    }

    fun read(
        path: String,
    ): ExecutableRunnerManifest {
        val normalized =
            Path
                .of(
                    path,
                ).toAbsolutePath()
                .normalize()
        val source = Json.parseToJsonElement(Files.readString(normalized).removePrefix(UTF8_BOM)).jsonObject
        val manifest =
            source.toManifest(
                path = normalized.toString(),
            )
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
                "executionScopes",
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
        val expectedHash =
            Sha256Hash.of(
                value =
                    CanonicalJson.stringify(
                        value = JsonObject(source.filterKeys { key -> key != "manifestHash" }),
                    ),
            )
        require(manifest.manifestHash == expectedHash) {
            "MCP manifest hash mismatch: ${manifest.manifestHash} != $expectedHash."
        }
        return manifest
    }

    fun hash(
        manifest: ExecutableRunnerManifest,
    ): String =
        Sha256Hash.of(
            value =
                CanonicalJson.stringify(
                    value =
                        manifest.toJson(
                            includeHash = false,
                        ),
                ),
        )

    private fun ExecutableRunnerManifest.toJson(
        includeHash: Boolean,
    ): JsonObject =
        buildJsonObject {
            put(
                "schemaVersion",
                schemaVersion,
            )
            put(
                "mode",
                mode,
            )
            put(
                "entrypoint",
                entrypoint,
            )
            put(
                "target",
                target,
            )
            put(
                "targets",
                targets.toJsonArray(),
            )
            put(
                "writeMetadata",
                writeMetadata,
            )
            put(
                "transport",
                transport,
            )
            put(
                "namespace",
                namespace,
            )
            put(
                "sectionNodeId",
                sectionNodeId?.let(::JsonPrimitive) ?: JsonNull,
            )
            put(
                "roots",
                roots.toJsonArray(),
            )
            put(
                "allowCanonicalSections",
                allowCanonicalSections,
            )
            put(
                "allowPartial",
                allowPartial,
            )
            put(
                "fullVisualSync",
                fullVisualSync,
            )
            put(
                "metadataPageId",
                metadataPageId,
            )
            put(
                "modelPath",
                modelPath,
            )
            put(
                "scriptPath",
                scriptPath,
            )
            put(
                "modelHash",
                modelHash,
            )
            put(
                "gitSha",
                gitSha,
            )
            put(
                "designModelLength",
                designModelLength,
            )
            put(
                "scriptLength",
                scriptLength,
            )
            put(
                "writerHash",
                writerHash,
            )
            put(
                "transportHash",
                transportHash,
            )
            put(
                "targetFingerprints",
                targetFingerprints.toJsonObject(),
            )
            put(
                "writerScopeFingerprints",
                writerScopeFingerprints.toJsonObject(),
            )
            put(
                "writerScopeFingerprintSchemaVersion",
                writerScopeFingerprintSchemaVersion,
            )
            put(
                "executionScopes",
                executionScopes.toJsonObject(),
            )
            put(
                "payloadImage",
                payloadImage?.toJson() ?: JsonNull,
            )
            put(
                "files",
                files.toJsonArray(),
            )
            put(
                "fileHashes",
                fileHashes.toJsonObject(),
            )
            if (includeHash) {
                put(
                    "manifestHash",
                    manifestHash,
                )
            }
        }

    private fun JsonObject.toManifest(
        path: String,
    ): ExecutableRunnerManifest =
        ExecutableRunnerManifest(
            path = path,
            schemaVersion =
                requiredInt(
                    name = "schemaVersion",
                ),
            mode =
                requiredString(
                    name = "mode",
                ),
            entrypoint =
                requiredString(
                    name = "entrypoint",
                ),
            target =
                requiredString(
                    name = "target",
                ),
            targets =
                requiredStringList(
                    name = "targets",
                ),
            writeMetadata =
                requiredBoolean(
                    name = "writeMetadata",
                ),
            transport =
                requiredString(
                    name = "transport",
                ),
            namespace =
                requiredString(
                    name = "namespace",
                ),
            sectionNodeId = this["sectionNodeId"]?.jsonPrimitive?.contentOrNull,
            roots =
                requiredStringList(
                    name = "roots",
                ),
            allowCanonicalSections =
                requiredBoolean(
                    name = "allowCanonicalSections",
                ),
            allowPartial =
                requiredBoolean(
                    name = "allowPartial",
                ),
            fullVisualSync =
                requiredBoolean(
                    name = "fullVisualSync",
                ),
            metadataPageId =
                requiredString(
                    name = "metadataPageId",
                ),
            modelPath =
                requiredString(
                    name = "modelPath",
                ),
            scriptPath =
                requiredString(
                    name = "scriptPath",
                ),
            modelHash =
                requiredString(
                    name = "modelHash",
                ),
            gitSha =
                requiredString(
                    name = "gitSha",
                ),
            designModelLength =
                requiredInt(
                    name = "designModelLength",
                ),
            scriptLength =
                requiredInt(
                    name = "scriptLength",
                ),
            writerHash =
                requiredString(
                    name = "writerHash",
                ),
            transportHash =
                requiredString(
                    name = "transportHash",
                ),
            targetFingerprints =
                stringMapOrEmpty(
                    name = "targetFingerprints",
                ),
            writerScopeFingerprints =
                stringMapOrEmpty(
                    name = "writerScopeFingerprints",
                ),
            writerScopeFingerprintSchemaVersion =
                intOrZero(
                    name = "writerScopeFingerprintSchemaVersion",
                ),
            executionScopes =
                stringMapOrEmpty(
                    name = "executionScopes",
                ),
            payloadImage =
                this["payloadImage"]
                    ?.takeUnless { value ->
                        value === JsonNull
                    }?.jsonObject
                    ?.toPayloadImage(),
            files =
                requiredStringList(
                    name = "files",
                ),
            fileHashes =
                requiredStringMap(
                    name = "fileHashes",
                ),
            manifestHash =
                requiredString(
                    name = "manifestHash",
                ),
        )

    private fun RunnerPayloadImage.toJson(): JsonObject =
        buildJsonObject {
            put(
                "fileName",
                fileName,
            )
            put(
                "byteLength",
                byteLength,
            )
            put(
                "sha256",
                sha256,
            )
            put(
                "textKeyword",
                textKeyword,
            )
        }

    private fun JsonObject.toPayloadImage(): RunnerPayloadImage =
        RunnerPayloadImage(
            fileName =
                requiredString(
                    name = "fileName",
                ),
            byteLength =
                requiredInt(
                    name = "byteLength",
                ),
            sha256 =
                requiredString(
                    name = "sha256",
                ),
            textKeyword =
                requiredString(
                    name = "textKeyword",
                ),
        )

    private fun JsonObject.requiredString(
        name: String,
    ): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.requiredInt(
        name: String,
    ): Int =
        this[name]?.jsonPrimitive?.int
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.requiredBoolean(
        name: String,
    ): Boolean =
        this[name]?.jsonPrimitive?.boolean
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.requiredStringList(
        name: String,
    ): List<String> =
        this[name]?.jsonArray?.map { value -> value.jsonPrimitive.content }
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.requiredStringMap(
        name: String,
    ): Map<String, String> =
        this[name]?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content }
            ?: throw IllegalArgumentException("MCP manifest is missing '$name'.")

    private fun JsonObject.stringMapOrEmpty(
        name: String,
    ): Map<String, String> =
        this[name]?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content }.orEmpty()

    private fun JsonObject.intOrZero(
        name: String,
    ): Int = this[name]?.jsonPrimitive?.int ?: 0

    private fun List<String>.toJsonArray(): JsonArray =
        JsonArray(
            map(
                transform = ::JsonPrimitive,
            ),
        )

    private fun Map<String, String>.toJsonObject(): JsonObject =
        JsonObject(mapValues { (_, value) -> JsonPrimitive(value) })

    private companion object {
        const val MINIMUM_SCHEMA_VERSION = 4
        const val UTF8_BOM = "\uFEFF"
        val prettyJson = Json { prettyPrint = true }
    }
}

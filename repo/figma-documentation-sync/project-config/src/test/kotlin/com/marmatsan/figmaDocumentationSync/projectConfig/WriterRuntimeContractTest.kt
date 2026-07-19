package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.figmaDocumentationSync.data.fingerprint.WriterScopeFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterProjectConfigJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterRuntimeConfigJson
import com.marmatsan.figmaDocumentationSync.data.writer.OfficialMcpRunnerGenerator
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class WriterRuntimeContractTest : FunSpec({
    test("keeps the language-neutral writer runtime contract executable in Kotlin") {
        val contractPath = Path.of(
            requireNotNull(System.getProperty("figmaDocumentationSyncWriterRuntimeContract"))
        )
        val contract = Json.parseToJsonElement(Files.readString(contractPath)).jsonObject
        val runtimeConfig = FigmaWriterRuntimeConfigJson.decode(
            FigmaWriterProjectConfigJson.encode(WaterMyPlantsFigmaWriterProjectConfig.value)
        )
        val visual = contract.getValue("visual").jsonObject
        val metadata = contract.getValue("metadata").jsonObject

        contract.getValue("schemaVersion").jsonPrimitive.content.toInt() shouldBe 1
        contract.getValue("manifestSchemaVersion").jsonPrimitive.content.toInt() shouldBe
            OfficialMcpRunnerGenerator.MANIFEST_SCHEMA_VERSION
        contract.getValue("writerScopeFingerprintSchemaVersion").jsonPrimitive.content.toInt() shouldBe
            WriterScopeFingerprintCalculator.SCHEMA_VERSION
        visual.getValue("targets").jsonArray.map { value -> value.jsonPrimitive.content } shouldContainExactly
            runtimeConfig.visualTargetNames
        metadata.getValue("targets").jsonArray.map { value -> value.jsonPrimitive.content } shouldContainExactly
            listOf("metadata")
        visual.getValue("transport").jsonPrimitive.content shouldBe OfficialMcpRunnerGenerator.TRANSPORT_PNG
        metadata.getValue("transport").jsonPrimitive.content shouldBe OfficialMcpRunnerGenerator.TRANSPORT_PNG

        val output = Files.createTempFile("writer-runtime-contract", ".json")
        try {
            val hash = "sha256:" + "a".repeat(64)
            val manifest = ExecutableRunnerManifestJson().finalizeAndWrite(
                draft = ExecutableRunnerManifest(
                    path = "",
                    schemaVersion = OfficialMcpRunnerGenerator.MANIFEST_SCHEMA_VERSION,
                    mode = "official",
                    entrypoint = "trunk-sync",
                    target = "metadata",
                    targets = listOf("metadata"),
                    writeMetadata = true,
                    transport = OfficialMcpRunnerGenerator.TRANSPORT_PNG,
                    namespace = runtimeConfig.officialStagingNamespace,
                    sectionNodeId = null,
                    roots = emptyList(),
                    allowOfficialSections = false,
                    fullVisualSync = false,
                    allowPartial = false,
                    metadataPageId = runtimeConfig.metadataPageId,
                    modelPath = "design-model.json",
                    scriptPath = "sync-trunk-design-model.mcp.js",
                    modelHash = hash,
                    gitSha = "contract-git-sha",
                    designModelLength = 1,
                    scriptLength = 1,
                    writerHash = hash,
                    transportHash = hash,
                    targetFingerprints = mapOf("metadata" to hash),
                    writerScopeFingerprints = mapOf("metadata" to hash),
                    writerScopeFingerprintSchemaVersion = WriterScopeFingerprintCalculator.SCHEMA_VERSION,
                    executionScopes = mapOf("99-run-target.mcp.js" to "metadata"),
                    payloadImage = null,
                    files = emptyList(),
                    fileHashes = emptyMap(),
                    manifestHash = ""
                ),
                outputPath = output.toString()
            )
            val serialized = Json.parseToJsonElement(Files.readString(output)).jsonObject
            val requiredFields = contract.getValue("requiredManifestFields").jsonArray
                .map { value -> value.jsonPrimitive.content }
            serialized.keys.toList() shouldContainExactly requiredFields
            contract.getValue("hashFields").jsonArray.forEach { field ->
                serialized.getValue(field.jsonPrimitive.content).jsonPrimitive.content shouldMatch
                    Regex("^sha256:[a-f0-9]{64}$")
            }
            manifest.manifestHash shouldBe serialized.getValue("manifestHash").jsonPrimitive.content
        } finally {
            Files.deleteIfExists(output)
        }
    }
})

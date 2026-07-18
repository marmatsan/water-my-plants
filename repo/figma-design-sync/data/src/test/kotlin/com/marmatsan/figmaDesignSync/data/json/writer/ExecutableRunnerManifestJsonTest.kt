package com.marmatsan.figmaDesignSync.data.json.writer

import com.marmatsan.figmaDesignSync.data.hash.Sha256Hash
import com.marmatsan.figmaDesignSync.data.json.CanonicalJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class ExecutableRunnerManifestJsonTest : FunSpec({
    test("reads legacy schema 2 manifests using their original hash body") {
        val body = buildJsonObject {
            put("schemaVersion", 2)
            put("mode", "official")
            put("entrypoint", "trunk-sync")
            put("target", "preflight")
            put("targets", buildJsonArray { add(JsonPrimitive("preflight")) })
            put("writeMetadata", false)
            put("transport", "chunks")
            put("namespace", "legacy_staging")
            put("sectionNodeId", kotlinx.serialization.json.JsonNull)
            put("roots", buildJsonArray { })
            put("allowOfficialSections", false)
            put("allowPartial", false)
            put("fullVisualSync", true)
            put("metadataPageId", "1:2")
            put("modelPath", "design-model.json")
            put("scriptPath", "writer.mcp.js")
            put("modelHash", "model-hash")
            put("gitSha", "git-sha")
            put("designModelLength", 10)
            put("scriptLength", 20)
            put("writerHash", "writer-hash")
            put("transportHash", "transport-hash")
            put("payloadImage", kotlinx.serialization.json.JsonNull)
            put("files", buildJsonArray { add(JsonPrimitive("99-run-target.mcp.js")) })
            put("fileHashes", buildJsonObject { put("99-run-target.mcp.js", "file-hash") })
        }
        val manifestHash = Sha256Hash.of(CanonicalJson.stringify(body))
        val source = JsonObject(body + ("manifestHash" to JsonPrimitive(manifestHash)))
        val file = Files.createTempFile("legacy-mcp-manifest", ".json")
        Files.writeString(file, source.toString())

        val manifest = ExecutableRunnerManifestJson().read(file.toString())

        manifest.schemaVersion shouldBe 2
        manifest.manifestHash shouldBe manifestHash
        manifest.executionScopes shouldBe emptyMap()
        manifest.writerScopeFingerprints shouldBe emptyMap()
        Files.deleteIfExists(file)
    }
})

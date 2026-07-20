package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncDecision
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncIdentity
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlan
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlanBody
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class VisualSyncPlanJsonTest : FunSpec(
    {
    test("matches the canonical Node plan hash contract") {
        VisualSyncPlanJson().hash(body) shouldBe
            "sha256:d9091312e43a19325de59bf9f53623e86e5caecd9180b41ad63494617cf97a3a"
    }

    test("writes the complete plan with its canonical hash") {
        val output = Files.createTempFile(
            "visual-sync-plan",
            ".json"
        )
        try {
            val adapter = VisualSyncPlanJson()
            val hash = adapter.hash(body)
            adapter.write(
                VisualSyncPlan(
                    body = body,
                    planHash = hash
                ),
                output.toString()
            )

            val json = Json.parseToJsonElement(Files.readString(output)).jsonObject
            json.getValue("decision").jsonPrimitive.content shouldBe "partial"
            json.getValue("planHash").jsonPrimitive.content shouldBe hash
        } finally {
            Files.deleteIfExists(output)
        }
    }

    test("normalizes string-encoded Figma fingerprint metadata") {
        val metadata = FigmaSyncMetadataJson.read(
            sharedPluginData = mapOf(
                "sync" to mapOf(
                    "modelHash" to "sha256:model",
                    "writerHash" to "sha256:writer",
                    "targetFingerprints" to "{\"versions\":\"sha256:target\"}",
                    "writerScopeFingerprints" to "{\"versions\":\"sha256:scope\"}",
                    "writerScopeFingerprintSchemaVersion" to "1"
                )
            ),
            namespace = "sync"
        )

        metadata?.targetFingerprints shouldBe mapOf("versions" to "sha256:target")
        metadata?.writerScopeFingerprints shouldBe mapOf("versions" to "sha256:scope")
        metadata?.writerScopeFingerprintSchemaVersion shouldBe 1
    }
}
)

private val body = VisualSyncPlanBody(
    schemaVersion = 1,
    decision = VisualSyncDecision.PARTIAL,
    reason = "target-model-fingerprints-changed",
    requiresVisualWrite = true,
    requiresMetadataWrite = true,
    executionScopes = listOf(
        "preflight",
        "waterMyPlants.libraries.androidx"
    ),
    identity = VisualSyncIdentity(
        modelHash = "sha256:model-new",
        writerHash = "sha256:writer-new",
        transportHash = "sha256:transport-new",
        writerScopeFingerprintSchemaVersion = 1
    ),
    manifestHash = "sha256:manifest"
)

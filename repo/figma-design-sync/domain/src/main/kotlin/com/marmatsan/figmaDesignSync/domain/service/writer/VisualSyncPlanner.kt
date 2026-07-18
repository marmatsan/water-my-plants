package com.marmatsan.figmaDesignSync.domain.service.writer

import com.marmatsan.figmaDesignSync.domain.model.writer.FigmaSyncMetadata
import com.marmatsan.figmaDesignSync.domain.model.writer.RunnerManifest
import com.marmatsan.figmaDesignSync.domain.model.writer.VisualSyncDecision
import com.marmatsan.figmaDesignSync.domain.model.writer.VisualSyncIdentity
import com.marmatsan.figmaDesignSync.domain.model.writer.VisualSyncPlan
import com.marmatsan.figmaDesignSync.domain.model.writer.VisualSyncPlanBody
import com.marmatsan.figmaDesignSync.domain.port.writer.VisualSyncPlanHasher

/** Selects full, partial, or no visual work from scoped model and writer identities. */
class VisualSyncPlanner(
    private val planHasher: VisualSyncPlanHasher
) {
    fun create(manifest: RunnerManifest, previousMetadata: FigmaSyncMetadata?): VisualSyncPlan {
        val allScopes = manifest.executionScopes.values.toList()
        val requiredWriterScopes = (allScopes + "metadata").distinct()
        require(
            manifest.targetFingerprints.isNotEmpty() &&
                hasFingerprintEntries(manifest.writerScopeFingerprints, requiredWriterScopes)
        ) {
            "Current MCP manifest is missing complete model-target or writer-scope fingerprints."
        }
        val identity = VisualSyncIdentity(
            modelHash = manifest.modelHash,
            writerHash = manifest.writerHash,
            transportHash = manifest.transportHash,
            writerScopeFingerprintSchemaVersion = manifest.writerScopeFingerprintSchemaVersion
        )

        if (previousMetadata == null) {
            return plan(VisualSyncDecision.FULL, "figma-metadata-unavailable", identity, allScopes, manifest)
        }
        if (
            previousMetadata.writerHash.isNullOrBlank() ||
            previousMetadata.targetFingerprints.isNullOrEmpty() ||
            previousMetadata.writerScopeFingerprintSchemaVersion == null ||
            !hasFingerprintEntries(previousMetadata.writerScopeFingerprints, requiredWriterScopes)
        ) {
            return plan(
                VisualSyncDecision.FULL,
                "legacy-metadata-without-execution-fingerprints",
                identity,
                allScopes,
                manifest
            )
        }
        if (
            previousMetadata.writerScopeFingerprintSchemaVersion !=
            manifest.writerScopeFingerprintSchemaVersion
        ) {
            return plan(
                VisualSyncDecision.FULL,
                "writer-scope-fingerprint-schema-changed",
                identity,
                allScopes,
                manifest
            )
        }

        val modelChanged = previousMetadata.modelHash != manifest.modelHash
        val writerChanged = previousMetadata.writerHash != manifest.writerHash
        if (!modelChanged && !writerChanged) {
            return plan(VisualSyncDecision.NONE, "visual-input-unchanged", identity, emptyList(), manifest)
        }

        val modelChangedScopes = if (modelChanged) {
            allScopes.filter { scope ->
                scope != "preflight" &&
                    manifest.targetFingerprints[scope] != previousMetadata.targetFingerprints.get(scope)
            }
        } else {
            emptyList()
        }
        if (modelChanged && modelChangedScopes.isEmpty()) {
            return plan(
                VisualSyncDecision.FULL,
                "model-changed-outside-known-target-fingerprints",
                identity,
                allScopes,
                manifest
            )
        }

        val writerChangedScopes = if (writerChanged) {
            allScopes.filter { scope ->
                manifest.writerScopeFingerprints[scope] !=
                    previousMetadata.writerScopeFingerprints?.get(scope)
            }
        } else {
            emptyList()
        }
        val metadataWriterChanged = writerChanged &&
            manifest.writerScopeFingerprints["metadata"] !=
            previousMetadata.writerScopeFingerprints?.get("metadata")
        if (writerChanged && writerChangedScopes.isEmpty() && !metadataWriterChanged) {
            return plan(
                VisualSyncDecision.FULL,
                "writer-changed-outside-known-scope-fingerprints",
                identity,
                allScopes,
                manifest
            )
        }
        if (writerChangedScopes.size == allScopes.size) {
            return plan(VisualSyncDecision.FULL, "shared-visual-writer-changed", identity, allScopes, manifest)
        }

        val scopes = (listOf("preflight") + modelChangedScopes + writerChangedScopes).distinct()
        val reason = when {
            modelChanged && writerChanged -> "target-model-and-writer-fingerprints-changed"
            writerChanged && metadataWriterChanged && writerChangedScopes.isEmpty() ->
                "metadata-writer-fingerprint-changed"
            writerChanged -> "writer-scope-fingerprints-changed"
            else -> "target-model-fingerprints-changed"
        }
        return plan(VisualSyncDecision.PARTIAL, reason, identity, scopes, manifest)
    }

    private fun plan(
        decision: VisualSyncDecision,
        reason: String,
        identity: VisualSyncIdentity,
        executionScopes: List<String>,
        manifest: RunnerManifest
    ): VisualSyncPlan {
        val body = VisualSyncPlanBody(
            schemaVersion = 1,
            decision = decision,
            reason = reason,
            requiresVisualWrite = decision != VisualSyncDecision.NONE,
            requiresMetadataWrite = decision != VisualSyncDecision.NONE,
            executionScopes = executionScopes,
            identity = identity,
            manifestHash = manifest.manifestHash
        )
        return VisualSyncPlan(body = body, planHash = planHasher.hash(body))
    }

    private fun hasFingerprintEntries(
        fingerprints: Map<String, String>?,
        scopes: List<String>
    ): Boolean = fingerprints != null && scopes.all { scope -> fingerprints[scope].isNullOrBlank().not() }
}

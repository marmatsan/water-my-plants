package com.marmatsan.figmaDocumentationSync.domain.service.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaSyncMetadata
import com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncDecision
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncIdentity
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlan
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlanBody
import com.marmatsan.figmaDocumentationSync.domain.port.writer.VisualSyncPlanHasher

/** Selects full, partial, or no visual work from scoped model and writer identities. */
class VisualSyncPlanner(
    private val planHasher: VisualSyncPlanHasher
) {
    /** Creates a hashed full, partial, or no-op plan from current and previous identities. */
    fun create(
        manifest: RunnerManifest,
        previousMetadata: FigmaSyncMetadata?
    ): VisualSyncPlan {
        val allScopes = manifest.executionScopes.values.toList()
        val requiredWriterScopes = (allScopes + "metadata").distinct()
        require(
            manifest.targetFingerprints.isNotEmpty() &&
                hasFingerprintEntries(
                    fingerprints = manifest.writerScopeFingerprints,
                    scopes = requiredWriterScopes
                )
        ) {
            "Current MCP manifest is missing complete model-target or writer-scope fingerprints."
        }
        val identity =
            VisualSyncIdentity(
                modelHash = manifest.modelHash,
                writerHash = manifest.writerHash,
                transportHash = manifest.transportHash,
                writerScopeFingerprintSchemaVersion = manifest.writerScopeFingerprintSchemaVersion
            )

        if (previousMetadata == null) {
            return plan(
                decision = VisualSyncDecision.FULL,
                reason = "figma-metadata-unavailable",
                identity = identity,
                executionScopes = allScopes,
                manifest = manifest
            )
        }
        if (
            previousMetadata.writerHash.isNullOrBlank() ||
            previousMetadata.targetFingerprints.isNullOrEmpty() ||
            previousMetadata.writerScopeFingerprintSchemaVersion == null ||
            !hasFingerprintEntries(
                fingerprints = previousMetadata.writerScopeFingerprints,
                scopes = requiredWriterScopes
            )
        ) {
            return plan(
                decision = VisualSyncDecision.FULL,
                reason = "legacy-metadata-without-execution-fingerprints",
                identity = identity,
                executionScopes = allScopes,
                manifest = manifest
            )
        }
        if (
            previousMetadata.writerScopeFingerprintSchemaVersion !=
            manifest.writerScopeFingerprintSchemaVersion
        ) {
            return plan(
                decision = VisualSyncDecision.FULL,
                reason = "writer-scope-fingerprint-schema-changed",
                identity = identity,
                executionScopes = allScopes,
                manifest = manifest
            )
        }

        val modelChanged = previousMetadata.modelHash != manifest.modelHash
        val compiledWriterChanged = previousMetadata.writerHash != manifest.writerHash
        val writerChangedScopes =
            allScopes.filter { scope ->
                manifest.writerScopeFingerprints[scope] !=
                    previousMetadata.writerScopeFingerprints?.get(
                        key = scope
                    )
            }
        val metadataWriterChanged =
            manifest.writerScopeFingerprints["metadata"] !=
                previousMetadata.writerScopeFingerprints?.get(
                    key = "metadata"
                )
        val writerChanged = compiledWriterChanged || writerChangedScopes.isNotEmpty() || metadataWriterChanged
        if (!modelChanged && !writerChanged) {
            return plan(
                decision = VisualSyncDecision.NONE,
                reason = "visual-input-unchanged",
                identity = identity,
                executionScopes = emptyList(),
                manifest = manifest
            )
        }

        val modelChangedScopes =
            if (modelChanged) {
                allScopes.filter { scope ->
                    scope != "preflight" &&
                        manifest.targetFingerprints[scope] !=
                        previousMetadata.targetFingerprints.get(
                            key = scope
                        )
                }
            } else {
                emptyList()
            }
        if (modelChanged && modelChangedScopes.isEmpty()) {
            return plan(
                decision = VisualSyncDecision.FULL,
                reason = "model-changed-outside-known-target-fingerprints",
                identity = identity,
                executionScopes = allScopes,
                manifest = manifest
            )
        }

        if (compiledWriterChanged && writerChangedScopes.isEmpty() && !metadataWriterChanged) {
            return plan(
                decision = VisualSyncDecision.FULL,
                reason = "writer-changed-outside-known-scope-fingerprints",
                identity = identity,
                executionScopes = allScopes,
                manifest = manifest
            )
        }
        if (writerChangedScopes.size == allScopes.size) {
            return plan(
                decision = VisualSyncDecision.FULL,
                reason = "shared-visual-writer-changed",
                identity = identity,
                executionScopes = allScopes,
                manifest = manifest
            )
        }

        val scopes = (listOf("preflight") + modelChangedScopes + writerChangedScopes).distinct()
        val reason =
            when {
                modelChanged && writerChanged -> {
                    "target-model-and-writer-fingerprints-changed"
                }

                writerChanged && metadataWriterChanged && writerChangedScopes.isEmpty() -> {
                    "metadata-writer-fingerprint-changed"
                }

                writerChanged -> {
                    "writer-scope-fingerprints-changed"
                }

                else -> {
                    "target-model-fingerprints-changed"
                }
            }
        return plan(
            decision = VisualSyncDecision.PARTIAL,
            reason = reason,
            identity = identity,
            executionScopes = scopes,
            manifest = manifest
        )
    }

    private fun plan(
        decision: VisualSyncDecision,
        reason: String,
        identity: VisualSyncIdentity,
        executionScopes: List<String>,
        manifest: RunnerManifest
    ): VisualSyncPlan {
        val body =
            VisualSyncPlanBody(
                schemaVersion = 1,
                decision = decision,
                reason = reason,
                requiresVisualWrite = decision != VisualSyncDecision.NONE,
                requiresMetadataWrite = decision != VisualSyncDecision.NONE,
                executionScopes = executionScopes,
                identity = identity,
                manifestHash = manifest.manifestHash
            )
        return VisualSyncPlan(
            body = body,
            planHash = planHasher.hash(body)
        )
    }

    private fun hasFingerprintEntries(
        fingerprints: Map<String, String>?,
        scopes: List<String>
    ): Boolean = fingerprints != null && scopes.all { scope -> fingerprints[scope].isNullOrBlank().not() }
}

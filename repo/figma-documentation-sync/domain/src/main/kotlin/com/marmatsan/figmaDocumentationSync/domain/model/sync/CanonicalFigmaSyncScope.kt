package com.marmatsan.figmaDocumentationSync.domain.model.sync

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaImpact
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVerificationScope

/**
 * Machine identity shared by the canonical Figma Sync generation and verification jobs.
 *
 * @property scope required verification depth for the change.
 * @property figmaImpact highest detected Figma impact.
 * @property affectedVisualTargets visual targets selected by impact classification.
 * @property comparisonBase Git revision used as the change-classification base.
 * @property gitSha canonical Git revision being synchronized.
 * @property modelHash design-model hash for a full verification scope.
 * @property writerHash hash of writer behavior affecting visual output.
 * @property transportHash hash of MCP transport behavior.
 * @property targetFingerprints visual target fingerprints keyed by target.
 * @property writerScopeFingerprints writer-scope fingerprints keyed by scope.
 * @property writerScopeFingerprintSchemaVersion fingerprint calculation schema version.
 * @property visualRunnerManifestHash canonical visual runner manifest hash.
 * @property metadataRunnerManifestHash canonical metadata runner manifest hash.
 * @property visualSyncDecision selected visual synchronization decision.
 * @property visualSyncPlanHash canonical visual synchronization plan hash.
 */
data class CanonicalFigmaSyncScope(
    val scope: FigmaVerificationScope,
    val figmaImpact: FigmaImpact,
    val affectedVisualTargets: List<String>,
    val comparisonBase: String?,
    val gitSha: String,
    val modelHash: String?,
    val writerHash: String?,
    val transportHash: String?,
    val targetFingerprints: Map<String, String>?,
    val writerScopeFingerprints: Map<String, String>?,
    val writerScopeFingerprintSchemaVersion: Int?,
    val visualRunnerManifestHash: String?,
    val metadataRunnerManifestHash: String?,
    val visualSyncDecision: String?,
    val visualSyncPlanHash: String?
)

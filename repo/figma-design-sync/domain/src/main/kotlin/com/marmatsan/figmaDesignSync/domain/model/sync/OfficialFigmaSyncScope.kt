package com.marmatsan.figmaDesignSync.domain.model.sync

import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaImpact
import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaVerificationScope

/** Machine identity shared by the official Figma Sync generation and verification jobs. */
data class OfficialFigmaSyncScope(
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

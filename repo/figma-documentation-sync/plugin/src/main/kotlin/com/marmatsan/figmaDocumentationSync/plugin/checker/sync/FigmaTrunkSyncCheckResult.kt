package com.marmatsan.figmaDocumentationSync.plugin.checker.sync

/**
 * Metadata read from Figma after a successful trunk sync check.
 *
 * @property modelHash canonical model hash confirmed in Figma.
 * @property gitSha repository revision confirmed in Figma.
 */
internal data class FigmaTrunkSyncCheckResult(
    val modelHash: String,
    val gitSha: String
)
